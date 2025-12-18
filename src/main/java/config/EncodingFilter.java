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

@WebFilter(filterName = "encodingFilter", urlPatterns = {"/*"}, initParams = {
	@WebInitParam(name = "encoding", value = "UTF-8")})
public class EncodingFilter implements Filter {

	private String encoding = "UTF-8";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		String encodingParam = filterConfig.getInitParameter("encoding");
		if (encodingParam != null && !encodingParam.isEmpty()) {
			this.encoding = encodingParam;
		}
		System.out.println("[EncodingFilter] 초기화 완료 - 인코딩: " + this.encoding);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse res = (HttpServletResponse)response;

		if (req.getCharacterEncoding() == null) {
			req.setCharacterEncoding(encoding);
		}
		res.setCharacterEncoding(encoding);

		String uri = req.getRequestURI();
		String ctx = req.getContextPath();

		// 정적 리소스 여부 확인 (제외 필요)
		boolean isStaticResource = uri.startsWith(ctx + "/static/")
			|| uri.matches(".*\\.(css|js|png|jpg|jpeg|gif|svg|ico|woff|woff2|map)$");

		if (!isStaticResource) {
			if (req.getCharacterEncoding() == null) {
				req.setCharacterEncoding(encoding);
			}
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