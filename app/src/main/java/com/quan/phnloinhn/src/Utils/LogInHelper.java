package com.quan.phnloinhn.src.Utils;

import android.content.Context;
import android.widget.Toast;

import com.firebase.ui.auth.ErrorCodes;

import java.util.HashMap;
import java.util.Map;

public class LogInHelper {
    public static final int VALID_CREDENTIALS = 0;
    public static final int INVALID_EMAIL = 1;
    public static final int INVALID_PASSWORD = 2;
    public static final Map<Integer, String> GOOGLEUI_MSGS = new HashMap<>();
    public static final Map<String, String> PASSWORD_MSGS = new HashMap<>();

    static {
        GOOGLEUI_MSGS.put(ErrorCodes.ANONYMOUS_UPGRADE_MERGE_CONFLICT, "Xung đột khi nâng cấp tài khoản ẩn danh.");
        GOOGLEUI_MSGS.put(ErrorCodes.DEVELOPER_ERROR, "Lỗi lập trình. Vui lòng kiểm tra lại mã nguồn.");
        GOOGLEUI_MSGS.put(ErrorCodes.EMAIL_MISMATCH_ERROR, "Sai email khi đăng nhập lại.");
        GOOGLEUI_MSGS.put(ErrorCodes.NO_NETWORK, "Không có kết nối mạng. Vui lòng kiểm tra mạng của bạn.");
        GOOGLEUI_MSGS.put(ErrorCodes.PLAY_SERVICES_UPDATE_CANCELLED, "Người dùng đã hủy cập nhật Google Play Services.");
        GOOGLEUI_MSGS.put(ErrorCodes.PROVIDER_ERROR, "Lỗi từ nhà cung cấp đăng nhập bên ngoài.");
        GOOGLEUI_MSGS.put(ErrorCodes.UNKNOWN_ERROR, "Đã xảy ra lỗi không xác định.");
    }

    static {
        PASSWORD_MSGS.put("ERROR_INVALID_EMAIL","Địa chỉ email không hợp lệ.");
        PASSWORD_MSGS.put("ERROR_EMAIL_ALREADY_IN_USE","Email đã được sử dụng.");
        PASSWORD_MSGS.put("ERROR_WEAK_PASSWORD","Mật khẩu quá yếu. Vui lòng chọn mật khẩu mạnh hơn.");
        PASSWORD_MSGS.put("ERROR_OPERATION_NOT_ALLOWED","Tài khoản email/password chưa được kích hoạt.");
        PASSWORD_MSGS.put("ERROR_USER_DISABLED","Tài khoản người dùng đã bị vô hiệu hóa.");
        PASSWORD_MSGS.put("ERROR_TOO_MANY_REQUESTS","Bạn đã gửi quá nhiều yêu cầu. Vui lòng thử lại sau.");
        PASSWORD_MSGS.put("ERROR_INVALID_CREDENTIAL", "Sai email hoặc mật khẩu");
    }

    public static int validateCredentials(Context context, String email, String password) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z.-]+$";
        // At least 8 characters, 1 uppercase, 1 digit, 1 special character
        String passwordRegex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=\\-{}\\[\\]:;\"'<>,.?/]).{8,}$";
        // Validate credentials : 0 - All True, 1 - Email invalid, 2 - Password invalid
        if (!email.matches(emailRegex)) {
//            Toast.makeText(context, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return INVALID_EMAIL;
        }

        if (!password.matches(passwordRegex)) {
//            Toast.makeText(context, "Mật khẩu cần ít nhất 8 ký tự, 1 chữ in hoa, 1 số và 1 ký tự đặc biệt", Toast.LENGTH_LONG).show();
            return INVALID_PASSWORD;
        }

        return VALID_CREDENTIALS;
    }

    public static int validateEmail(Context context, String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z.-]+$";
        // Validate credentials : 0 - True, 1 - Email invalid
        if (!email.matches(emailRegex)) {
//            Toast.makeText(context, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return INVALID_EMAIL;
        }
        return VALID_CREDENTIALS;
    }
}
