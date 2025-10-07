package com.example.DATN.security;

public final class Endpoints {

    private Endpoints() {}

    // FE dev domain (đổi theo môi trường)
    public static final String FRONTEND_HOST = "http://localhost:5173";

    // Swagger / OpenAPI
    public static final String[] SWAGGER = {
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
    };

    /* ------------------ PUBLIC (không cần JWT) ------------------ */

    // GET public: xem phòng đã duyệt, tra cứu địa lý, danh mục
    public static final String[] PUBLIC_GET = {
            "/rooms",              // list approved
            "/rooms/**",           // detail nếu có
            "/areas/provinces",
            "/areas/wards",
            "/amenities",
            "/room-types",
            "/user/activate/**",   // hoặc /user/active-account/** nếu bạn dùng tên này
            "/public/**",          // static assets (nếu có)
            "/assets/**",
            "/user/me",

    };

    // POST public: đăng ký, đăng nhập, refresh token (nếu có)
    public static final String[] PUBLIC_POST = {
            "/user/register",
            "/user/authenticate",
            "/user/refresh"

    };

    // PUT public: quên mật khẩu (nếu muốn để public)
    public static final String[] PUBLIC_PUT = {
            "/user/forgot-password"
    };

    // DELETE public: thường không có
    public static final String[] PUBLIC_DELETE = { };


    /* ------------------ ROLE-BASED ------------------ */

    // Admin namespace: duyệt/từ chối phòng, quản trị danh mục…
    public static final String[] ADMIN_ENDPOINT = {
            "/admin/**",
            "/admin/room-types",
            "/admin/amenity",
    };

    // Host namespace: tạo/sửa phòng (đăng bài) – quyền HOST
    public static final String[] HOST_ENDPOINT = {
            "/host/**"
    };
}
