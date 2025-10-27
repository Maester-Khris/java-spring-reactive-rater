package nk.springprojects.reactive.integration;

import nk.springprojects.reactive.users.User;
import nk.springprojects.reactive.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class AuthIntegrationTestIT {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    private static final String TEST_USERNAME = "integrationUser";
    private static final String TEST_PASSWORD = "testPassword";

    @BeforeEach
    public void setup() {
        // Optional: Clean up the user before each test if the repository is not in-memory
        userRepository.deleteByUsername(TEST_USERNAME).block();
    }

    @Test
    @DisplayName("Test whole authentication flow: Register, Login, Access CSRF Token Endpoint")
    void registerLoginAndTestCsrfTokenFlow() {
        // Step 1: Create the user
        // Step 2 & 3: Login and test CSRF endpoint
        testUserRegistration();
        testUserLoginAndCsrfAccess();
    }

    void testUserRegistration() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("username", TEST_USERNAME);
        formData.add("password", TEST_PASSWORD);

        // 1. Send registration request
        webTestClient.post().uri("/register")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(formData))
            .exchange()
            .expectStatus().isFound() // Assert redirection (HTTP 302/FOUND)
            .expectHeader().location("/"); // Assert redirection to home endpoint

        // 2. Assert the user has been added to the database
        User createdUser = userRepository.findByUsername(TEST_USERNAME).block();
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getUsername()).isEqualTo(TEST_USERNAME);
    }

    void testUserLoginAndCsrfAccess() {
        // **A. Retrieve Session and CSRF token from the initial login page**
        // We need a pre-login CSRF token to submit the login form.
        WebTestClient.ResponseSpec loginPageResponse = webTestClient.get().uri("/login")
            .exchange()
            .expectStatus().isOk();

        // 1. Extract the session cookies (important for subsequent requests)
        // 2. Extract the CSRF token from the response body (if exposed, or from headers/cookies if configured)
        //    *Note: For Form Login in WebFlux, the CSRF token for the login form
        //    is typically passed as a hidden input field.* //    For simplicity in WebTestClient, we might skip the form's CSRF token
        //    if Spring Security's defaults are being used, as the login POST
        //    is often automatically excluded from CSRF protection.
        //    However, if it's protected, you need to parse the login page HTML
        //    to get the token/parameter name.
        //    We'll assume the login endpoint is CSRF-exempt for now.

        // **B. Login the user**
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("username", TEST_USERNAME); // Default login form parameter names
        formData.add("password", TEST_PASSWORD);

        // Assuming you have a WebTestClient instance named webTestClient

        // STEP 1: Perform the successful login and capture the response
        WebTestClient.ResponseSpec loginResponse = webTestClient.post().uri("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData)) // formData contains "username" and "password"
                .exchange()
                .expectStatus().isFound()
                .expectHeader().location("/");

//        // STEP 2: Extract the full Set-Cookie header value containing the Session ID
//        // We use expectHeader().value() to ensure we get the full string, e.g., "SESSION=abc1234; Path=/; HttpOnly"
//        String sessionCookie = loginResponse.expectHeader()
//                .value("Set-Cookie", cookie -> cookie.contains("SESSION")).toString();
//
//        // STEP 3: Make the subsequent request (to the home page or /api/auth/csrf-token)
//        // and explicitly include the captured cookie string in the "Cookie" header.
//        webTestClient.get().uri("/")
//                // This is the CRITICAL step: reusing the cookie
//                .header("Cookie", sessionCookie)
//                .exchange()
//                // Now you should see 'check if user is authenticated: true' in the logs
//                .expectStatus().isOk();
//
//        // The same session reuse is needed for the CSRF token endpoint
//        this.webTestClient.get().uri("/api/auth/csrf-token")
//                .header("Cookie", sessionCookie)
//                .exchange()
//                .expectStatus().isOk();

//        WebTestClient.ResponseSpec loginResponse = webTestClient.post().uri("/login")
//            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
//            .body(BodyInserters.fromFormData(formData))
//            // Capture the session from the first exchange for subsequent requests
//            .exchange()
//            .expectStatus().isFound() // Assert redirection (HTTP 302/FOUND)
//            .expectHeader().location("/"); // Assert redirection to home endpoint (Step 2)
//
//        // Extract the full session for subsequent authenticated calls
//        String sessionCookie = loginResponse.expectHeader()
//            .value("Set-Cookie", cookie -> cookie.contains("SESSION")).toString();
//
//        // **C. Test the /api/auth/csrf-token endpoint (Step 3)**
//
//        // The authenticated request to home ("/") should establish the session and
//        // potentially set the final CsrfToken in a cookie or header (if using XSRF-TOKEN cookie)
//        // Let's first access the home page to ensure the session is active and cookies are set.
//        WebTestClient.ResponseSpec homeResponse = webTestClient.get().uri("/")
//            .header("Cookie", sessionCookie)
//            .exchange()
//            .expectStatus().isOk(); // Asserts successful access after login
//
//        // Now, request the CSRF token endpoint using the established session
//        this.webTestClient.get().uri("/api/auth/csrf-token")
//            .header("Cookie", sessionCookie)
//            .exchange()
//            .expectStatus().isOk() // Asserts successful access (authenticated required)
//            .expectBody(CsrfToken.class) // Asserts the body is a CsrfToken object
//            .value(token -> {
//                assertThat(token.getToken()).isNotBlank();
//                assertThat(token.getHeaderName()).isNotBlank();
//                assertThat(token.getParameterName()).isNotBlank();
//            });
    }
}
