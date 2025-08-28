package com.example.phnloinhn.src.Helper;

import com.firebase.ui.auth.ErrorCodes;

import java.util.HashMap;
import java.util.Map;

public class LogInHelper {
    public static final Map<Integer, String> GOOGLEUI_MSGS = new HashMap<>() {{
        put(ErrorCodes.ANONYMOUS_UPGRADE_MERGE_CONFLICT, "Xung đột khi nâng cấp tài khoản ẩn danh.");
        put(ErrorCodes.DEVELOPER_ERROR, "Lỗi lập trình. Vui lòng kiểm tra lại mã nguồn.");
        put(ErrorCodes.EMAIL_MISMATCH_ERROR, "Sai email khi đăng nhập lại.");
        put(ErrorCodes.NO_NETWORK, "Không có kết nối mạng. Vui lòng kiểm tra mạng của bạn.");
        put(ErrorCodes.PLAY_SERVICES_UPDATE_CANCELLED, "Người dùng đã hủy cập nhật Google Play Services.");
        put(ErrorCodes.PROVIDER_ERROR, "Lỗi từ nhà cung cấp đăng nhập bên ngoài.");
        put(ErrorCodes.UNKNOWN_ERROR, "Đã xảy ra lỗi không xác định.");
    }};

    public static final Map<String, String> PASSWORD_MSGS = new HashMap<>(){{
        PASSWORD_MSGS.put("ERROR_INVALID_EMAIL","Địa chỉ email không hợp lệ.");
        PASSWORD_MSGS.put("ERROR_EMAIL_ALREADY_IN_USE","Email đã được sử dụng.");
        PASSWORD_MSGS.put("ERROR_WEAK_PASSWORD","Mật khẩu quá yếu. Vui lòng chọn mật khẩu mạnh hơn.");
        PASSWORD_MSGS.put("ERROR_OPERATION_NOT_ALLOWED","Tài khoản email/password chưa được kích hoạt.");
        PASSWORD_MSGS.put("ERROR_USER_DISABLED","Tài khoản người dùng đã bị vô hiệu hóa.");
        PASSWORD_MSGS.put("ERROR_TOO_MANY_REQUESTS","Bạn đã gửi quá nhiều yêu cầu. Vui lòng thử lại sau.");
    }};
}
