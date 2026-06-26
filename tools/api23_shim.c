/* api23_shim — backfill the libc functions Bionic only added at API 24+, so a CPython
 * built for API 24+ (e.g. youtubedl-android's bundled python) resolves its symbols on an
 * API-23 (Android 6.0) device. Add this as a DT_NEEDED of libpython (or LD_PRELOAD it).
 *
 * Per-ABI gap vs API-23 Bionic (from `nm` analysis of youtubedl-android 0.17.3):
 *   arm64-v8a / x86_64 : lockf  preadv  pwritev
 *   armeabi-v7a / x86  : lockf64 preadv64 pwritev64   (32-bit large-file variants)
 * We define both the off_t and off64_t variants so one source covers every ABI. The
 * getifaddrs/if_nameindex/memfd_create stubs are kept defensively (newer CPython builds
 * pull them in). preadv/pwritev are implemented via pread64/pwrite64 loops — correct and
 * arch-independent (no raw-syscall offset-ABI pitfalls). pread64/pwrite64 exist on API 23. */
#include <sys/uio.h>
#include <sys/types.h>
#include <unistd.h>
#include <fcntl.h>
#include <stdlib.h>
#include <sys/syscall.h>

ssize_t pread64(int, void *, size_t, off64_t);
ssize_t pwrite64(int, const void *, size_t, off64_t);

#ifndef F_ULOCK
#define F_ULOCK 0
#define F_LOCK  1
#define F_TLOCK 2
#define F_TEST  3
#endif

static int lockf_impl(int fd, int cmd, off64_t len) {
    struct flock fl;
    fl.l_whence = SEEK_CUR; fl.l_start = 0; fl.l_len = (off_t) len;
    if (cmd == F_ULOCK) { fl.l_type = F_UNLCK; return fcntl(fd, F_SETLK,  &fl); }
    if (cmd == F_LOCK)  { fl.l_type = F_WRLCK; return fcntl(fd, F_SETLKW, &fl); }
    if (cmd == F_TLOCK) { fl.l_type = F_WRLCK; return fcntl(fd, F_SETLK,  &fl); }
    if (cmd == F_TEST)  { fl.l_type = F_WRLCK;
        if (fcntl(fd, F_GETLK, &fl) < 0) return -1;
        return fl.l_type == F_UNLCK ? 0 : -1; }
    return -1;
}
int lockf(int fd, int cmd, off_t len)     { return lockf_impl(fd, cmd, len); }
int lockf64(int fd, int cmd, off64_t len) { return lockf_impl(fd, cmd, len); }

static ssize_t preadv_impl(int fd, const struct iovec *iov, int n, off64_t off) {
    ssize_t total = 0;
    for (int i = 0; i < n; i++) {
        ssize_t r = pread64(fd, iov[i].iov_base, iov[i].iov_len, off);
        if (r < 0) return total ? total : -1;
        total += r; off += r;
        if ((size_t) r < iov[i].iov_len) break;
    }
    return total;
}
ssize_t preadv(int fd, const struct iovec *iov, int n, off_t off)     { return preadv_impl(fd, iov, n, off); }
ssize_t preadv64(int fd, const struct iovec *iov, int n, off64_t off) { return preadv_impl(fd, iov, n, off); }

static ssize_t pwritev_impl(int fd, const struct iovec *iov, int n, off64_t off) {
    ssize_t total = 0;
    for (int i = 0; i < n; i++) {
        ssize_t r = pwrite64(fd, iov[i].iov_base, iov[i].iov_len, off);
        if (r < 0) return total ? total : -1;
        total += r; off += r;
        if ((size_t) r < iov[i].iov_len) break;
    }
    return total;
}
ssize_t pwritev(int fd, const struct iovec *iov, int n, off_t off)     { return pwritev_impl(fd, iov, n, off); }
ssize_t pwritev64(int fd, const struct iovec *iov, int n, off64_t off) { return pwritev_impl(fd, iov, n, off); }

/* network-interface enumeration (added API 24) — benign stubs, kept defensively */
struct ifaddrs;
int  getifaddrs(struct ifaddrs **ifap) { if (ifap) *ifap = 0; return 0; }
void freeifaddrs(struct ifaddrs *ifa)  { (void) ifa; }
struct if_nameindex { unsigned int if_index; char *if_name; };
struct if_nameindex *if_nameindex(void) { return (struct if_nameindex *) calloc(1, sizeof(struct if_nameindex)); }
void if_freenameindex(struct if_nameindex *p) { free(p); }

/* memfd_create (added API 30) — defensive (some CPython builds reference it) */
int memfd_create(const char *name, unsigned int flags) {
    return (int) syscall(SYS_memfd_create, name, flags);
}
