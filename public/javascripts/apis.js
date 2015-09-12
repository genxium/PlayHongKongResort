function apiCDNDomain(provider) {
        if (provider == g_cdnQiniu) {
                return "/image/cdn/qiniu/domain";
        }
        return null;
}