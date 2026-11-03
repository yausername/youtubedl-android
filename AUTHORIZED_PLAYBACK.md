# Authorized playback sample

This Android sample is intended only for content you own, content licensed to you, or a provider's official API/SDK. It does not extract links from third-party VIP pages or bypass subscriptions, access controls, or DRM.

## Configure the app

1. In `PlaybackApiClient.java`, set `BASE_URL` to your own HTTPS backend URL (keep the trailing slash).
2. Connect your existing sign-in flow to `new SessionManager(context).saveAccessToken(accessToken)`. Call `clear()` on logout. The access token is encrypted using an AES key held by Android Keystore; never ship a backend service secret in the APK.
3. The playback screen calls `GET /api/videos/{videoId}/playback` with `Authorization: Bearer <user access token>`.
4. Return a JSON envelope such as:

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

The API must authenticate the user, check entitlement for the requested video, and issue a short-lived URL for content the user is authorized to watch. Enforce expiry and authorization at the origin/CDN too; `expiresAt` in the response is informational, not a security check. For HLS/DASH, make sure segment and manifest requests are authorized (for example via provider-supported signed cookies/URLs or the configured bearer header). Avoid redirecting credentials across origins.

For DRM media, use the content provider's official DRM/license integration. This sample intentionally does not implement license extraction or DRM circumvention. Test the Gradle build against your configured Android SDK and backend; `api.example.com` is a placeholder.
