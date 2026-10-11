# Admin test cases

Automated unit coverage currently exercises account status guardrails in `backend/src/test/java/edu/campusnest/service/AdminServiceTest.java`.

| Case | Expected result | Current verification |
|---|---|---|
| Last active admin suspension | Rejected and no audit row written | Automated unit test |
| Admin suspends own account | Rejected and no audit row written | Automated unit test |
| Unknown account filter | Rejected as invalid input | Automated unit test |
| Anonymous request to `/api/admin/dashboard` | `401` | Not run against a started server |
| Signed-in USER request to admin API | `403` | Not run against a started server |
| Dashboard counts on empty MySQL schema | Zero values and empty activity | Not run against MySQL |
| Owner creates a property | Listing persisted as PENDING | Not run against MySQL |
| Public search hides pending/rejected listings | Only approved and available rows returned | Not run against MySQL |
| Approve/reject operation | Status persists with audit record | Not run against MySQL |
| Suspend account | Status persists, sessions rejected on subsequent request | Not run against MySQL |
| Admin user/property search and pagination | Filters and page totals match real rows | Not run against MySQL |
| Responsive UI | Desktop and mobile layouts remain usable | JavaScript syntax checked; browser layout not verified |

Local execution requires MySQL and the bootstrap admin environment variables described in `README.md`. Never record a demo as passed until it has been exercised against the running database.
