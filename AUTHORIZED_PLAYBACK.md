# Authorized playback sample

This Android sample is intended only for content you own, content licensed to you, or a provider's official API/SDK. It does not extract links from third-party VIP pages or bypass subscriptions, access controls, or DRM.

## Configure the app

1. In `PlaybackApiClient.java`, set `BASE_URL` to your own HTTPS backend URL (keep the trailing slash).
2. The sample login screen posts JSON `{ "username": "...", "password": "..." }` to `POST /api/auth/login`. The backend should return `{ "code": 0, "message": "ok", "data": { "token": "..." } }` after verifying credentials. Connect your real identity provider/backend; do not hardcode demo credentials or secrets in the app.
3. The returned user access token is encrypted using an AES key held by Android Keystore. Sign out removes the stored token.
4. The playback screen calls `GET /api/videos/{videoId}/playback` with `Authorization: Bearer <user access token>`.
5. Return a JSON envelope such as:

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "playUrl": "https://cdn.your-domain.example/path/master.m3u8?short_lived_signature=...",
    "mimeType": "application/vnd.apple.mpegurl",
    "expiresAt": "2026-11-03T12:00:00Z"
  }
}
```

The API must authenticate the user, check entitlement for the requested video, and issue a short-lived URL for content the user is authorized to watch. Enforce expiry and authorization at the origin/CDN too; `expiresAt` in the response is informational, not a security check. The app deliberately does not forward its backend bearer token to the media/CDN host; use provider-approved signed URLs/cookies or an explicitly designed media authorization flow. Ensure redirects and segment requests remain authorized without leaking backend credentials across origins.

For DRM media, use the content provider's official DRM/license integration. This sample intentionally does not implement license extraction or DRM circumvention. Test the Gradle build against your configured Android SDK and backend; `api.example.com` is a placeholder.
