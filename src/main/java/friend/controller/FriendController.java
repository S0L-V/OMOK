package friend.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import friend.dto.FriendAcceptDTO;
import friend.dto.FriendDTO;
import friend.dto.FriendRequestDTO;
import friend.service.FriendService;
import friend.service.FriendServiceImpl;

@WebServlet("/friend/*")
public class FriendController extends HttpServlet {

	private FriendService friendService;
	private Gson gson;

	@Override
	public void init() throws ServletException {
		try {
			this.friendService = new FriendServiceImpl();
			this.gson = new Gson();
		} catch (Exception e) {
			throw new ServletException("FriendController 초기화 실패");
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();

		if (pathInfo == null || pathInfo.equals("/")) {
			sendError(res, 400, "잘못된 요청입니다.");
			return;
		}

		String action = pathInfo.substring(1);
		String userId = getLoginUserId(req);

		if (userId == null) {
			sendError(res, 401, "로그인이 필요합니다.");
			return;
		}

		switch (action) {
			case "list":
				handleGetFriendList(res, userId);
				break;
			case "pending":
				handleGetPendingRequests(res, userId);
				break;
			default:
				sendError(res, 404, "존재하지 않는 API입니다.");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();

		if (pathInfo == null || pathInfo.equals("/")) {
			sendError(res, 400, "잘못된 요청입니다.");
			return;
		}

		String action = pathInfo.substring(1);
		String userId = getLoginUserId(req);

		if (userId == null) {
			sendError(res, 401, "로그인이 필요합니다.");
			return;
		}

		switch (action) {
			case "request":
				handleSendRequest(req, res, userId);
				break;
			case "accept":
				handleAcceptRequest(req, res, userId);
				break;
			case "reject":
				handleRejectRequest(req, res, userId);
				break;
			case "remove":
				handleRemoveFriend(req, res, userId);
				break;
			case "block":
				handleBlockFriend(req, res, userId);
				break;
			default:
				sendError(res, 404, "존재하지 않는 API입니다.");
		}
	}

	/**
	 * GET /friend/list - 내 친구 목록 조회
	 */
	private void handleGetFriendList(HttpServletResponse res, String userId) throws IOException {
		List<FriendDTO> friends = friendService.getMyFriends(userId);
		sendSuccess(res, friends);
	}

	/**
	 * GET /friend/pending - 나에게 온 친구 요청 목록
	 */
	private void handleGetPendingRequests(HttpServletResponse res, String userId) throws IOException {
		List<FriendDTO> requests = friendService.getPendingRequests(userId);
		sendSuccess(res, requests);
	}

	/**
	 * POST /friend/reject - 친구 요청 거절
	 */
	private void handleRejectRequest(HttpServletRequest req, HttpServletResponse res, String userId) throws
		IOException {
		FriendAcceptDTO body = gson.fromJson(req.getReader(), FriendAcceptDTO.class);

		if (body == null || body.getRequesterId() == null || body.getRequesterId().isBlank()) {
			sendError(res, 400, "requesterId가 필요합니다.");
			return;
		}

		boolean success = friendService.rejectFriendRequest(body.getRequesterId(), userId);

		if (success) {
			sendSuccess(res, "친구 요청을 거절했습니다.");
		} else {
			sendError(res, 400, "친구 요청 거절에 실패했습니다.");
		}
	}

	/**
	 * POST /friend/request - 친구 요청 보내기
	 */
	private void handleSendRequest(HttpServletRequest req, HttpServletResponse res, String userId) throws IOException {
		FriendRequestDTO body = gson.fromJson(req.getReader(), FriendRequestDTO.class);

		if (body == null || body.getFriendId() == null || body.getFriendId().isBlank()) {
			sendError(res, 400, "friendId가 필요합니다.");
			return;
		}

		boolean success = friendService.sendFriendRequest(userId, body.getFriendId());

		if (success) {
			sendSuccess(res, "친구 요청을 보냈습니다.");
		} else {
			sendError(res, 400, "친구 요청에 실패했습니다.");
		}
	}

	/**
	 * POST /friend/accept - 친구 요청 수락
	 */
	private void handleAcceptRequest(HttpServletRequest req, HttpServletResponse res, String userId) throws
		IOException {
		FriendAcceptDTO body = gson.fromJson(req.getReader(), FriendAcceptDTO.class);

		if (body == null || body.getRequesterId() == null || body.getRequesterId().isBlank()) {
			sendError(res, 400, "requesterId가 필요합니다.");
			return;
		}

		boolean success = friendService.acceptFriendRequest(body.getRequesterId(), userId);

		if (success) {
			sendSuccess(res, "친구 요청을 수락했습니다.");
		} else {
			sendError(res, 400, "친구 요청 수락에 실패했습니다.");
		}
	}

	/**
	 * POST /friend/remove - 친구 삭제
	 */
	private void handleRemoveFriend(HttpServletRequest req, HttpServletResponse res, String userId) throws IOException {
		FriendRequestDTO body = gson.fromJson(req.getReader(), FriendRequestDTO.class);

		if (body == null || body.getFriendId() == null || body.getFriendId().isBlank()) {
			sendError(res, 400, "friendId가 필요합니다.");
			return;
		}

		boolean success = friendService.removeFriend(userId, body.getFriendId());

		if (success) {
			sendSuccess(res, "친구를 삭제했습니다.");
		} else {
			sendError(res, 400, "친구 삭제에 실패했습니다.");
		}
	}

	/**
	 * POST /friend/block - 친구 차단
	 */
	private void handleBlockFriend(HttpServletRequest req, HttpServletResponse res, String userId) throws IOException {
		FriendRequestDTO body = gson.fromJson(req.getReader(), FriendRequestDTO.class);

		if (body == null || body.getFriendId() == null || body.getFriendId().isBlank()) {
			sendError(res, 400, "friendId가 필요합니다.");
			return;
		}

		boolean success = friendService.blockFriend(userId, body.getFriendId());

		if (success) {
			sendSuccess(res, "친구를 차단했습니다.");
		} else {
			sendError(res, 400, "친구 차단에 실패했습니다.");
		}
	}

	/**
	 * 세션에서 로그인한 사용자 ID 가져오기
	 */
	private String getLoginUserId(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		if (session == null) {
			return null;
		}
		return (String)session.getAttribute("loginUserId");
	}

	/**
	 * 성공 응답
	 */
	private void sendSuccess(HttpServletResponse res, Object data) throws IOException {
		res.setStatus(200);
		ApiResponse response = new ApiResponse(true, data, null);
		res.getWriter().write(gson.toJson(response));
	}

	/**
	 * 에러 응답
	 */
	private void sendError(HttpServletResponse res, int statusCode, String message) throws IOException {
		res.setStatus(statusCode);
		ApiResponse response = new ApiResponse(false, null, message);
		res.getWriter().write(gson.toJson(response));
	}

	// API 응답용 내부 클래스
	private static class ApiResponse {
		boolean success;
		Object data;
		String message;

		ApiResponse(boolean success, Object data, String message) {
			this.success = success;
			this.data = data;
			this.message = message;
		}
	}
}
