package config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;

/**
 * UTF-8 인코딩 필터
 * @WebFilter로 자동 등록
 */
@WebFilter(
    filterName = "encodingFilter",
    urlPatterns = {"/*"}, 
    initParams = {
        @WebInitParam(name = "encoding", value = "UTF-8")
    }
)
public class EncodingFilter implements Filter {

    private String encoding = "UTF-8";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // web.xml에서 설정한 인코딩 값 가져오기
        String encodingParam = filterConfig.getInitParameter("encoding");
        if (encodingParam != null && !encodingParam.isEmpty()) {
            this.encoding = encodingParam;
        }
        System.out.println("[EncodingFilter] 초기화 완료 - 인코딩: " + this.encoding);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // 요청 인코딩 설정
        if (request.getCharacterEncoding() == null) {
            request.setCharacterEncoding(encoding);
        }

        // 응답 인코딩 설정
        response.setCharacterEncoding(encoding);
        response.setContentType("text/html; charset=" + encoding);

        // 다음 필터 또는 서블릿으로 전달
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        System.out.println("[EncodingFilter] 종료");
    }
}