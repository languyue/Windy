package com.zj.auth.handler;


import com.zj.auth.entity.Constants;
import com.zj.auth.entity.UserSession;
import com.zj.auth.service.TokenHolder;
import com.zj.contract.exception.ErrorCode;
import com.zj.contract.json.WireJson;
import com.zj.contract.rpc.ResponseMeta;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

    private final TokenHolder tokenHolder;

    public LogoutSuccessHandlerImpl(TokenHolder tokenHolder) {
        this.tokenHolder = tokenHolder;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        UserSession session = tokenHolder.getUserSessionByToken(request);
        if (Objects.nonNull(session)) {
            tokenHolder.deleteToken(session);
        }
        response.setStatus(HttpStatus.OK.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(Constants.JSON_MEDIA_TYPE);
        PrintWriter writer = response.getWriter();
        ResponseMeta responseMeta = new ResponseMeta(ErrorCode.SUCCESS);
        writer.print(WireJson.write(responseMeta));
        writer.flush();
    }
}
