package login.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter("/lobby")
public class LoginFilterChain implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse resp = (HttpServletResponse)response;

		HttpSession session = req.getSession(false);

		String loginUserId = (session != null) ? (String)session.getAttribute("loginUserId") : null;

		if (loginUserId == null) {
			resp.sendRedirect(req.getContextPath() + "/login.jsp?error=login_required");
			return;
		}

		chain.doFilter(request, response);
	}
}