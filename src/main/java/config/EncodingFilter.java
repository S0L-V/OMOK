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
import javax.servlet.http.HttpServletResponse;

/**
 * UTF-8 인코딩 필터
 * @WebFilter로 자동 등록
 */
@WebFilter(
    filterName = "encodingFilter",
    urlPatterns = {"/*"},
    initParams = { @WebInitParam(name = "encoding", value = "UTF-8") }
)
public class EncodingFilter implements Filter {

    private String encoding = "UTF-8";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // web.xml 또는 @WebInitParam으로 설정한 인코딩 값 가져오기
        String encodingParam = filterConfig.getInitParameter("encoding");
        if (encodingParam != null && !encodingParam.isEmpty()) {
            this.encoding = encodingParam;
        }
        System.out.println("[EncodingFilter] 초기화 완료 - 인코딩: " + this.encoding);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String ctx = req.getContextPath();

        // 정적 리소스 요청은 contentType 등을 굳이 건드리지 않도록 제외 (css/js/img/font 등)
        boolean isStaticResource =
                uri.startsWith(ctx + "/static/") ||
                uri.matches(".*\\.(css|js|png|jpg|jpeg|gif|svg|ico|woff|woff2|map)$");

        // 요청 인코딩은 항상 설정해도 무방하지만,
        // 이미 설정된 경우엔 건드리지 않음
        if (req.getCharacterEncoding() == null) {
            req.setCharacterEncoding(encoding);
        }

        // 정적 리소스가 아닌 경우에만 응답 쪽을 명확히 지정
        if (!isStaticResource) {
            res.setCharacterEncoding(encoding);
            res.setContentType("text/html; charset=" + encoding);
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        System.out.println("[EncodingFilter] 종료");
    }
}
