package com.zj.auth.handler;


import com.zj.auth.entity.Constants;
import com.zj.contract.exception.ErrorCode;
import com.zj.contract.json.WireJson;
import com.zj.contract.rpc.ResponseMeta;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthenticationFailHandlerImpl implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(Constants.JSON_MEDIA_TYPE);
        PrintWriter writer = response.getWriter();
        ResponseMeta responseMeta = new ResponseMeta(ErrorCode.USER_PASSWORD_ERROR);
        writer.print(WireJson.write(responseMeta));
        writer.flush();
    }
}
