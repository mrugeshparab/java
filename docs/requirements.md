# CampusNest initial requirements and implementation boundary

This is a proposed starting point for the three-member project. Shared data and login live in one Spring Boot app and one MySQL schema. The first milestone prioritizes authenticated admin overview, account status management, property moderation, inquiry visibility, and immutable-to-dashboard audit history.

Admin operations are enforced server-side. A normal account receives `403` on admin APIs; unauthenticated requests receive `401`. Registration cannot assign ADMIN. The last active admin cannot be suspended. Public listing queries must filter to approved and available records.

Implemented in this workspace: public property search, owner registration and property submission, listing photo upload, and admin review/moderation. Still not represented as complete: external messaging, production deployment, security audit, and end-to-end tests against a real shared MySQL instance.
