we are designing integration test for resiliency of a java springboot app. and right noz wew are experiencing error in test setup

our idea of the actual situation

the first test is failing because of inconsistent state of the voteque between the instance accessed in the test and the instance modified by the controller

actually the second test i failing because the ratelimiter is consumed bythe first test instead ofbeing reinitializedbefoe each test



test: vote process later

[skillrater] INFO | 2025-10-19 05:00:52.858 | parallel-3 | n.s.r.c.AppConfiguration:47 | [skillrater] INFO | Incoming request | POST | /api/v1/data/skill-vote | headers=[WebTestClient-Request-Id:"1", Content-Type:"application/json", Content-Length:"72"]

[skillrater] INFO | 2025-10-19 05:00:52.985 | boundedElastic-1 | n.s.r.c.SkillRatingRestController:99 | [skillrater] INFO | REST vote | IP=unknown | skillUUID=051ac915-a389-4090-892f-0bdece586da6 | type=UPVOTE

[skillrater] INFO | 2025-10-19 05:00:53.046 | parallel-1 | n.s.r.c.AppConfiguration:47 | [skillrater] INFO | Incoming request | POST | /api/v1/data/skill-vote | headers=[WebTestClient-Request-Id:"2", Content-Type:"application/json", Content-Length:"72"]

[skillrater] INFO | 2025-10-19 05:00:53.060 | boundedElastic-1 | n.s.r.c.SkillRatingRestController:99 | [skillrater] INFO | REST vote | IP=unknown | skillUUID=051ac915-a389-4090-892f-0bdece586da6 | type=UPVOTE

[skillrater] INFO | 2025-10-19 05:00:53.074 | parallel-3 | n.s.r.c.AppConfiguration:47 | [skillrater] INFO | Incoming request | POST | /api/v1/data/skill-vote | headers=[WebTestClient-Request-Id:"3", Content-Type:"application/json", Content-Length:"72"]

[skillrater] INFO | 2025-10-19 05:00:53.085 | boundedElastic-1 | n.s.r.c.SkillRatingRestController:99 | [skillrater] INFO | REST vote | IP=unknown | skillUUID=051ac915-a389-4090-892f-0bdece586da6 | type=UPVOTE

[skillrater] INFO | 2025-10-19 05:00:53.101 | parallel-1 | n.s.r.c.AppConfiguration:47 | [skillrater] INFO | Incoming request | POST | /api/v1/data/skill-vote | headers=[WebTestClient-Request-Id:"4", Content-Type:"application/json", Content-Length:"72"]

[skillrater] INFO | 2025-10-19 05:00:53.117 | boundedElastic-1 | n.s.r.c.SkillRatingRestController:99 | [skillrater] INFO | REST vote | IP=unknown | skillUUID=051ac915-a389-4090-892f-0bdece586da6 | type=UPVOTE

[skillrater] INFO | 2025-10-19 05:00:53.133 | parallel-3 | n.s.r.c.AppConfiguration:47 | [skillrater] INFO | Incoming request | POST | /api/v1/data/skill-vote | headers=[WebTestClient-Request-Id:"5", Content-Type:"application/json", Content-Length:"72"]

[skillrater] INFO | 2025-10-19 05:00:53.158 | boundedElastic-1 | n.s.r.c.SkillRatingRestController:99 | [skillrater] INFO | REST vote | IP=unknown | skillUUID=051ac915-a389-4090-892f-0bdece586da6 | type=UPVOTE

[skillrater] INFO | 2025-10-19 05:00:53.180 | parallel-1 | n.s.r.c.AppConfiguration:47 | [skillrater] INFO | Incoming request | POST | /api/v1/data/skill-vote | headers=[WebTestClient-Request-Id:"6", Content-Type:"application/json", Content-Length:"72"]

[skillrater] INFO | 2025-10-19 05:00:53.199 | boundedElastic-1 | n.s.r.c.SkillRatingRestController:99 | [skillrater] INFO | REST vote | IP=unknown | skillUUID=051ac915-a389-4090-892f-0bdece586da6 | type=UPVOTE

[skillrater] INFO | 2025-10-19 05:00:53.216 | parallel-3 | n.s.r.c.AppConfiguration:47 | [skillrater] INFO | Incoming request | POST | /api/v1/data/skill-vote | headers=[WebTestClient-Request-Id:"7", Content-Type:"application/json", Content-Length:"72"]

[skillrater] INFO | 2025-10-19 05:00:53.225 | boundedElastic-1 | n.s.r.c.SkillRatingRestController:99 | [skillrater] INFO | REST vote | IP=unknown | skillUUID=051ac915-a389-4090-892f-0bdece586da6 | type=UPVOTE

[skillrater] INFO | 2025-10-19 05:00:53.239 | parallel-1 | n.s.r.c.AppConfiguration:47 | [skillrater] INFO | Incoming request | POST | /api/v1/data/skill-vote | headers=[WebTestClient-Request-Id:"8", Content-Type:"application/json", Content-Length:"72"]

[skillrater] INFO | 2025-10-19 05:00:53.253 | boundedElastic-1 | n.s.r.c.SkillRatingRestController:99 | [skillrater] INFO | REST vote | IP=unknown | skillUUID=051ac915-a389-4090-892f-0bdece586da6 | type=UPVOTE

[skillrater] INFO | 2025-10-19 05:00:53.266 | parallel-3 | n.s.r.c.AppConfiguration:47 | [skillrater] INFO | Incoming request | POST | /api/v1/data/skill-vote | headers=[WebTestClient-Request-Id:"9", Content-Type:"application/json", Content-Length:"72"]

[skillrater] INFO | 2025-10-19 05:00:53.276 | boundedElastic-1 | n.s.r.c.SkillRatingRestController:99 | [skillrater] INFO | REST vote | IP=unknown | skillUUID=051ac915-a389-4090-892f-0bdece586da6 | type=UPVOTE

[skillrater] INFO | 2025-10-19 05:00:53.293 | parallel-1 | n.s.r.c.AppConfiguration:47 | [skillrater] INFO | Incoming request | POST | /api/v1/data/skill-vote | headers=[WebTestClient-Request-Id:"10", Content-Type:"application/json", Content-Length:"72"]

[skillrater] INFO | 2025-10-19 05:00:53.314 | boundedElastic-1 | n.s.r.c.SkillRatingRestController:99 | [skillrater] INFO | REST vote | IP=unknown | skillUUID=051ac915-a389-4090-892f-0bdece586da6 | type=UPVOTE



org.opentest4j.AssertionFailedError:

Expected :1

Actual   :0





test: rate limit exceeded

[skillrater] INFO | 2025-10-19 05:00:53.371 | parallel-3 | n.s.r.c.AppConfiguration:47 | [skillrater] INFO | Incoming request | POST | /api/v1/data/skill-vote | headers=[WebTestClient-Request-Id:"11", Content-Type:"application/json", Content-Length:"72"]

[skillrater] WARN | 2025-10-19 05:00:53.374 | parallel-3 | n.s.r.c.SkillRatingRestController:112 | [skillrater] WARN | Rate limit exceeded for IP=unknown

[skillrater] ERROR | 2025-10-19 05:00:53.392 | main | o.s.t.w.r.server.ExchangeResult:237 | Request details for assertion failure:



> POST /api/v1/data/skill-vote

> WebTestClient-Request-Id: [11]

> Content-Type: [application/json]

> Content-Length: [72]



{"voteType":"UPVOTE","skilluuid":"a2d1f5a4-8c55-44d3-b1c2-13c3310cc72d"}



< 429 TOO_MANY_REQUESTS Too Many Requests

< Content-Type: [text/plain;charset=UTF-8]

< Content-Length: [45]

< Cache-Control: [no-cache, no-store, max-age=0, must-revalidate]

< Pragma: [no-cache]

< Expires: [0]

< X-Content-Type-Options: [nosniff]

< X-Frame-Options: [DENY]

< X-XSS-Protection: [0]

< Referrer-Policy: [no-referrer]



Too many votes. Your request has been queued.





java.lang.AssertionError: Range for response status value 429 TOO_MANY_REQUESTS expected:<SUCCESSFUL> but was:<CLIENT_ERROR>

Expected :SUCCESSFUL

Actual   :CLIENT_ERROR