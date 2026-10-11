# Proposed API contract

Status: initial proposal, because the shared repository was empty when work began. Member 1 and Member 2 should review this before integrating their portals. All routes are same-origin under `/api`; JSON uses camelCase. Errors use `{ "code": "...", "message": "..." }`.

## Authentication

- `POST /api/auth/register`: `{fullName,email,password,phone?,role}`; role accepts `USER` or `OWNER` only. Returns `201` and a sanitized account DTO.
- `POST /api/auth/login`: `{email,password}`; establishes an HTTP session, returns the authenticated account DTO.
- `POST /api/auth/logout`: invalidates the session (CSRF token required).
- `GET /api/auth/me`: returns current account DTO; `401` when not signed in.
- Session cookie is HttpOnly and SameSite=Lax. State-changing requests require Spring Security's CSRF token in `X-CSRF-TOKEN`.
- Admin bootstrap is an operator action through environment variables, not public registration.

## Admin (ADMIN session required)

- `GET /api/admin/dashboard`: database-derived totals and latest audit activity.
- `GET /api/admin/users?page=0&size=20&search=&role=&status=`: paginated sanitized users.
- `GET /api/admin/users/{id}`: account details; no password or credential fields.
- `PATCH /api/admin/users/{id}/status`: `{status:"ACTIVE"|"SUSPENDED"}`. Cannot suspend the current admin or the last active admin.
- `GET /api/admin/properties?page=0&size=20&search=&listingStatus=&availabilityStatus=`: paginated listing rows with owner display name.
- `GET /api/admin/properties/{id}`: listing and owner details.
- `PATCH /api/admin/properties/{id}/approve`: approves a pending listing.
- `PATCH /api/admin/properties/{id}/reject`: `{reason}`; rejection reason is required.
- `PATCH /api/admin/properties/{id}/status`: `{status:"ACTIVE"|"INACTIVE"}`; retains record history.
- `GET /api/admin/inquiries?page=0&size=20&status=`: restricted admin view, paginated.
- `GET /api/admin/audit-logs?page=0&size=20`: read-only, newest first.

Page response shape: `{content:[],page,size,totalElements,totalPages}`. List sizes are capped at 100. Property public visibility is `listingStatus=APPROVED AND availabilityStatus=AVAILABLE`.

## Shared entity conventions

Roles: `USER`, `OWNER`, `ADMIN`. Account statuses: `ACTIVE`, `SUSPENDED`. Listing statuses: `PENDING`, `APPROVED`, `REJECTED`, `INACTIVE`. Availability: `AVAILABLE`, `UNAVAILABLE`. Passwords use BCrypt. Approval/rejection and account status updates plus audit writes are transactional. Approved-listing edits are expected to return the listing to `PENDING` when listing-edit APIs are added.

## Shared starter routes

- `GET /api/properties?page=0&size=20&city=`: public paginated search of approved and available listings only.
- `POST /api/owner/properties`: OWNER session required; creates a pending listing from validated property fields.
- `POST /api/user/inquiries`: USER session required; submits a message for a currently public listing.

## Not implemented / pending team agreement

Owners can upload up to eight JPEG, PNG, or WebP images per listing using `POST /api/owner/properties/{id}/images` as multipart field `images` (5 MB per image). Uploads are restricted to the listing owner. `GET /api/properties/{id}/images/{imageId}` serves approved/available listing images publicly and pending/rejected listing images only to their owner or an admin. Owner submission and image metadata share the same database as admin moderation. Bookmarks, owner responses, reviews, and reporting endpoints are not implemented.
