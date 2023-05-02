package org.hub.controller;

import javax.servlet.http.HttpSession;

import org.hub.domain.BoardVO;
import org.hub.domain.Criteria;
import org.hub.domain.FieldVO;
import org.hub.domain.PageDTO;
import org.hub.domain.StackVO;
import org.hub.domain.UserReadVO;
import org.hub.domain.UserVO;
import org.hub.interceptor.SessionNames;
import org.hub.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@Log4j
@RequestMapping("/board/*")
@AllArgsConstructor
public class BoardController {
	
	//public static final String LOGIN = "loginUser"; //이름이 loginUser인 세션
	
	@Autowired
	private BoardService service;
	
	@GetMapping("/register")
	public String registerView(HttpSession session) throws Exception {
		UserVO user = (UserVO)session.getAttribute(SessionNames.LOGIN);
		
		log.info("새 글쓰기로 이동");
		return "register";
	}
	
	@PostMapping("/register")
	public String registerProc(BoardVO board, HttpSession session, RedirectAttributes rttr) {
		System.out.println("글 등록 컨트롤러");
		System.out.println();
		System.out.println(session.getId());
		
		UserVO user = (UserVO)session.getAttribute(SessionNames.LOGIN);
		
		String uidkey = user.getUidKey();
		System.out.println(uidkey);
		board.setUidkey(uidkey);
		
		if(board.getFnames() != null) {
			String[] fnameList = board.getFnames().split(",");
			for(String fname : fnameList) {
				log.info(fname);
			}
		}
		
		if(board.getSnames() != null) {
			String[] snameList = board.getSnames().split(",");
			for(String sname : snameList) {
				log.info(sname);
			}
		}
		rttr.addFlashAttribute("registerResult", "글 등록완료");
		service.register(board);
		return "redirect:/board/main";
	}
	
	// 글 수정화면
	@GetMapping(value= "/modify")
	public String modifyView(@RequestParam("bno") int bno, @ModelAttribute("cri") Criteria cri, Model model) {
		log.info("/ modify");
		model.addAttribute("board", service.get(bno));
		model.addAttribute("cri", cri);
		log.info("cri 확인(0)[화면갈때] :" + cri);
		return "modify";			
	}
	
	// 글 수정 POST
	@PostMapping("/modify")
	public String modify(BoardVO board, @ModelAttribute("cri") Criteria cri, RedirectAttributes rttr) {
		log.info("글 수정 POST 컨트롤러 = = = = = ");
		log.info("수정 내용 반영된 board : " + board);
		log.info("cri 확인(1) :" + cri);
		// field 선택된 것 log로 확인
		if(board.getFnames() != null) {
			String[] fnameList = board.getFnames().split(",");
			for(String fname : fnameList) {
				log.info(fname);
			}
		}
		
		// stack 선택된 것 log로 확인
		if(board.getSnames() != null) {
			String[] snameList = board.getSnames().split(",");
			for(String sname : snameList) {
				log.info(sname);
			}
		}
		
		// service.modify() 호출
		boolean result = service.modify(board);
		if(result == true) {
			rttr.addFlashAttribute("modifyResult", "글 수정이 되었습니다.");
		}
		// 페이징 정보 URL이 붙여서 페이징 정보 유지하며 화면 이동
		log.info("cri 확인(2) :" + cri);
		return "redirect:/board/main" + cri.getListLink();
	}
	
	
	
	// 버튼 클릭 시 글 모집 마감 
	@GetMapping("/upClose")
	@ResponseBody
	public String updateStatus(@RequestParam("bno") int bno){			
		log.info("upClose..." + bno);
		boolean result = service.updateStatus(bno);
		if(result==true) {
			return "success";
		} else {
			return "fail"; 
		}
	}
	
	// 마감 처리 취소하기
	@GetMapping(value="/upOpen")
	@ResponseBody
	public String resetStatus(@RequestParam("bno") int bno) {
		log.info("resetStatus..." + bno);
		boolean result = service.resetStatus(bno);
		if(result==true) {
			return "success";
		} else {
			return "fail"; 
		}	
	}
	
	@GetMapping("/remove")
	public String remove(@RequestParam("bno") int bno, RedirectAttributes rttr) {
		log.info("remove..." + bno);
		if(service.remove(bno)) {
			rttr.addFlashAttribute("removeResult", "삭제완료");
		}

		
//		rttr.addAttribute("pageNum", cri.getPageNum());
//		rttr.addAttribute("amount", cri.getAmount());
//		rttr.addAttribute("type", cri.getType());
//		rttr.addAttribute("keyword", cri.getKeyword());
//		
		return "redirect:/board/main";
	}
	
	@PostMapping("/remove")
	public String remove(@RequestParam("bno") int bno, Criteria cri, RedirectAttributes rttr) {
		log.info("remove..." + bno);
		if(service.remove(bno)) {
			rttr.addFlashAttribute("removeResult", "삭제완료");
		}
//		rttr.addAttribute("pageNum", cri.getPageNum());
//		rttr.addAttribute("amount", cri.getAmount());
//		rttr.addAttribute("type", cri.getType());
//		rttr.addAttribute("keyword", cri.getKeyword());
//		
		return "redirect:/board/main" + cri.getListLink();
	}
	 
	// 글 상세보기
	@GetMapping(value="/get")
	public String get(@RequestParam("bno") int bno, @ModelAttribute("cri") Criteria cri, Model model,
			HttpSession session, UserReadVO userread) {
		log.info("/get");
		log.info("cri 확인(get화면)) :" + cri);
		model.addAttribute("board", service.get(bno));
		
		//userread_tbl 에 데이터 넣기 위함
		// 로그인한 사용자 정보 가져오기
	    UserVO user = (UserVO)session.getAttribute(SessionNames.LOGIN);
		if(user!=null){
			String uidKey = user.getUidKey();
			userread.setUidkey(uidKey);
			userread.setBno(bno);
			
			// 읽은글 중복 확인
			int result = service.bnoCheck(userread);
			log.info("결과값 = " + result);
			if(result == 0) {
				service.saveUserRead(userread);
			}
		}				
		
		return "get";	
		
	}		

	
	@GetMapping("/reply")
	public String getboard() {
		System.out.println("reply으로 이동");
		log.info("reply 이동");

		return "reply";
	}
}
