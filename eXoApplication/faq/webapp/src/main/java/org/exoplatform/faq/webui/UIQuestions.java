/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.faq.webui;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQServiceUtils;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.webui.popup.UICategoryForm;
import org.exoplatform.faq.webui.popup.UIDeleteQuestion;
import org.exoplatform.faq.webui.popup.UIMoveCategoryForm;
import org.exoplatform.faq.webui.popup.UIMoveQuestionForm;
import org.exoplatform.faq.webui.popup.UIPopupAction;
import org.exoplatform.faq.webui.popup.UIPopupContainer;
import org.exoplatform.faq.webui.popup.UIQuestionForm;
import org.exoplatform.faq.webui.popup.UIQuestionManagerForm;
import org.exoplatform.faq.webui.popup.UIResponseForm;
import org.exoplatform.faq.webui.popup.UISendMailForm;
import org.exoplatform.faq.webui.popup.UISettingForm;
import org.exoplatform.faq.webui.popup.UIWatchForm;
import org.exoplatform.faq.webui.popup.UIWatchManager;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@SuppressWarnings("unused")
@ComponentConfig(
		template =	"app:/templates/faq/webui/UIQuestions.gtmpl" ,
		events = {
			@EventConfig(listeners = UIQuestions.AddCategoryActionListener.class),
	    @EventConfig(listeners = UIQuestions.AddNewQuestionActionListener.class),
	    @EventConfig(listeners = UIQuestions.OpenCategoryActionListener.class),
	    @EventConfig(listeners = UIQuestions.EditSubCategoryActionListener.class),
	    @EventConfig(listeners = UIQuestions.EditCategoryActionListener.class),
	    @EventConfig(listeners = UIQuestions.DeleteCategoryActionListener.class, confirm= "UIQuestions.msg.confirm-delete-category"),
	    @EventConfig(listeners = UIQuestions.MoveCategoryActionListener.class),
	    @EventConfig(listeners = UIQuestions.MoveDownActionListener.class),
	    @EventConfig(listeners = UIQuestions.MoveUpActionListener.class),
	    @EventConfig(listeners = UIQuestions.SettingActionListener.class),
	    @EventConfig(listeners = UIQuestions.WatchActionListener.class),
	    @EventConfig(listeners = UIQuestions.WatchManagerActionListener.class),
      // action of question:
	    @EventConfig(listeners = UIQuestions.QuestionManagamentActionListener.class),
	    @EventConfig(listeners = UIQuestions.ViewQuestionActionListener.class),
	    @EventConfig(listeners = UIQuestions.ResponseQuestionActionListener.class),
	    @EventConfig(listeners = UIQuestions.EditQuestionActionListener.class),
	    @EventConfig(listeners = UIQuestions.DeleteQuestionActionListener.class),
	    @EventConfig(listeners = UIQuestions.MoveQuestionActionListener.class),
	    @EventConfig(listeners = UIQuestions.SendQuestionActionListener.class),
	    @EventConfig(listeners = UIQuestions.ChangeQuestionActionListener.class)
      
		}
)
public class UIQuestions extends UIContainer {
  private static String SEARCH_INPUT = "SearchInput" ;
  
  private FAQSetting faqSetting_ = null;
	private List<Category> categories_ = null ;
  private List<Boolean> categoryModerators = new ArrayList<Boolean>() ;
  public List<Question> listQuestion_ =  null ;
  private List<String> listCateId_ = new ArrayList<String>() ;
  private boolean canEditQuestion = false ;
  private String categoryId_ = null ;
  private String parentId_ = null ;
  public String questionView_ = "" ;
  public static String newPath_ = "" ;
  String currentUser_ = "";
	private static	FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
  public List<QuestionLanguage> listQuestionLanguage = new ArrayList<QuestionLanguage>() ;
  public boolean isChangeLanguage = false ;
  public List<String> listLanguage = new ArrayList<String>() ;
  public String backPath_ = "" ;
  private static String language_ = "" ;
  private List<String> emailList_ = new ArrayList<String>() ;
  
  private String[] secondTollbar_ = new String[]{"AddCategory", "AddNewQuestion", "QuestionManagament"} ;
  private String[] firstTollbar_ = new String[]{"AddCategory", "QuestionManagament"} ;
  private String[] firstActionCate_ = new String[]{"AddCategory", "AddNewQuestion", "EditCategory", "DeleteCategory", "MoveCategory", "MoveDown", "MoveUp", "Watch"} ;
  private String[] secondActionCate_ = new String[]{"AddCategory", "AddNewQuestion", "EditSubCategory", "DeleteCategory", "MoveCategory", "MoveDown", "MoveUp", "Watch"} ;
  private String[] userActionsCate_ = new String[]{"AddNewQuestion", "Watch"} ;
  private String[] moderatorActionQues_ = new String[]{"ResponseQuestion", "EditQuestion", "DeleteQuestion", "MoveQuestion", "SendQuestion"} ;
  private String[] userActionQues_ = new String[]{"SendQuestion"} ;
  private String[] sizes_ = new String[]{"bytes", "KB", "MB"};
  
	public UIQuestions()throws Exception {
    backPath_ = null ;
	  this.categoryId_ = new String() ;
    currentUser_ = FAQUtils.getCurrentUser() ;
    faqSetting_ = new FAQSetting() ;
		FAQUtils.getPorletPreference(faqSetting_);
		if(currentUser_ != null && currentUser_.trim().length() > 0){
			faqService.getUserSetting(FAQUtils.getSystemProvider(), currentUser_, faqSetting_);
		}
		addChild(UIQuickSearch.class, null, null) ;
    setCategories() ;
	}
  
  public String[] getActionTollbar() {
    return secondTollbar_ ;
  }
  
  public FAQSetting getFAQSetting(){
  	return faqSetting_;
  }
  
  public void setFAQSetting(FAQSetting setting){
  	this.faqSetting_ = setting;
  }
  
  @SuppressWarnings("unused")
  private String[] getFirstTollbar() {
    return firstTollbar_ ;
  }
  
  @SuppressWarnings("unused")
  private String[] getActionCategory(){
    return firstActionCate_ ;
  }
  
  private String[] getSecondActionCategory() {
    return secondActionCate_ ;
  }
  
  private String[] getActionCategoryWithUser() {
    return userActionsCate_ ;
  }
  
  
  @SuppressWarnings("unused")
  private String[] getActionQuestion(){
    if(canEditQuestion) {
      return moderatorActionQues_;
    } else {
      return userActionQues_;
    }
  }
  
  @SuppressWarnings("unused")
  private String[] getActionQuestionWithUser(){
    String[] action = new String[]{"SendQuestion"} ;
    return action ;
  }
  
  @SuppressWarnings("unused")
  private String getParentId(){
    return this.parentId_ ;
  }
  
  public void setParentId(String parentId_) {
    this.parentId_ = parentId_ ;
  }
  
  public void setCategories() throws Exception  {
    if(categories_ != null)
      categories_.clear() ;
    if(this.categoryId_ == null || this.categoryId_.trim().length() < 1) {
      categories_ = faqService.getSubCategories(null, FAQUtils.getSystemProvider(), this.faqSetting_) ;
    } else {
      categories_ = faqService.getSubCategories(this.categoryId_, FAQUtils.getSystemProvider(), this.faqSetting_) ;
      setIsModerators() ;
    }
  }
  
  @SuppressWarnings("static-access")
	public void setLanguageView(String language){
    this.language_ = language;
  }
	
  public void setCategories(String categoryId) throws Exception  {
  	setCategoryId(categoryId) ;
  }
  
  private void setIsModerators() {
    categoryModerators.clear() ;
    FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
    if(serviceUtils.isAdmin(currentUser_)) {
      canEditQuestion = true ;
      for(int i = 0 ; i < this.categories_.size(); i ++) {
        this.categoryModerators.add(true);
      }
    } else {
      if(categoryId_ == null || categoryId_.trim().length() < 1) {
        listCateId_.clear() ;
      } else {
        if(!listCateId_.contains(categoryId_)) {
          listCateId_.add(categoryId_) ;
        } else {
          int pos = listCateId_.indexOf(categoryId_) ;
          for(int i = pos + 1; i < listCateId_.size() ; i ++) {
            listCateId_.remove(i) ;
          }
        }
      }
      boolean isContinue = true ;
      if(listCateId_.size() > 0){
        for(String cateIdProcess : listCateId_) {
          try {
            if(Arrays.asList(faqService.getCategoryById(cateIdProcess, FAQUtils.getSystemProvider()).getModeratorsCategory()).contains(currentUser_)){
              for(int j = 0 ; j < categories_.size(); j ++) {
                categoryModerators.add(true) ;
              }
              isContinue = false ;
              canEditQuestion = true ;
              break ;
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
      
      if(isContinue) {
        canEditQuestion = false ;
        for(Category category : categories_) {
          try {
            if(Arrays.asList(category.getModeratorsCategory()).contains(currentUser_)) {
              categoryModerators.add(true) ;
            }else {
              categoryModerators.add(false) ;
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
      
    }
  }
  
  private List<Boolean> getIsModerator() {
    return this.categoryModerators ;
  }
  
  private String[] getActionWithCategory() {
    return null ;
  }
  
	@SuppressWarnings("unused")
  private List<Category> getCategories() throws Exception {
		return categories_ ;
	}
  //Need to make API to check this info, Do NOT do as now 
	@SuppressWarnings("unused")
	private long[] getCategoryInfo(String categoryId) {
    long[] result = new long[]{0, 0, 0, 0} ;
	  try {
      result = faqService.getCategoryInfo(categoryId, FAQUtils.getSystemProvider()) ;
	  } catch (Exception e) {
      e.printStackTrace() ;
    }
    return result ;
	}
	
  public void setListQuestion() {
    isChangeLanguage = false;
    try{
      SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
      if(!canEditQuestion) {
        this.listQuestion_ = faqService.getQuestionsByCatetory(categoryId_, sessionProvider, this.faqSetting_).getAll() ;
      } else {
        this.listQuestion_ = faqService.getAllQuestionsByCatetory(categoryId_, sessionProvider, this.faqSetting_).getAll() ;
      }
    } catch (FileNotFoundException fileNotFount) { 
      fileNotFount.printStackTrace() ;
    } catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  public void setListQuestion(List<Question> listQuestion) {
    this.listQuestion_ = listQuestion ;
  }
  
  private String convertSize(long size){
    String result = "";
    long  residual = 0;
    int i = 0;
    while(size >= 1000){
      i ++;
      residual = size % 1024;
      size /= 1024;
    }
    if(residual > 1000){
      String str = residual + "";
      result = size + "." + str.substring(0, 3) + " " + sizes_[i];
    }else{
      result = size + "." + residual + " " + sizes_[i];
    }
    return result;
  }
  
  @SuppressWarnings("unused")
  public List<Question> getListQuestion() {
    return this.listQuestion_ ;
  }
  
  private boolean getCanEditQuestion() {
    return this.canEditQuestion ;
  }
  
  @SuppressWarnings("unused")
  private String getQuestionView(){
    return this.questionView_ ;
  }
  
  private List<String> getQuestionLangauges(Question question){
    try {
      if(!isChangeLanguage) {
        listLanguage.clear() ;
        listQuestionLanguage.clear() ;
        
        QuestionLanguage quesLanguage = new QuestionLanguage() ;
        quesLanguage.setLanguage(question.getLanguage()) ;
        quesLanguage.setQuestion(question.getQuestion()) ;
        quesLanguage.setResponse(question.getResponses()) ;
        quesLanguage.setResponseBy(question.getResponseBy());
        quesLanguage.setDateResponse(question.getDateResponse());
        listQuestionLanguage.add(quesLanguage) ;
        
        listQuestionLanguage.addAll(faqService.getQuestionLanguages(question.getId(), FAQUtils.getSystemProvider())) ;
        for(QuestionLanguage questionLanguage : listQuestionLanguage) {
          listLanguage.add(questionLanguage.getLanguage()) ;
        }
      }
      return listLanguage ;
    } catch (Exception e) {
      e.printStackTrace() ;
    }
    return null ;
  }
  
  @SuppressWarnings("unused")  
  private String getFileSource(InputStream input, String fileName, DownloadService dservice) throws Exception {
    byte[] imageBytes = null;
    if (input != null) {
      imageBytes = new byte[input.available()];
      input.read(imageBytes);
      ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes);
      InputStreamDownloadResource dresource = new InputStreamDownloadResource(
          byteImage, "image");
      dresource.setDownloadName(fileName);
      return dservice.getDownloadLink(dservice.addDownloadResource(dresource));
    }
    return null;
  }
  
  private String getFileSource(FileAttachment attachment) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    try {
      InputStream input = attachment.getInputStream() ;
      String fileName = attachment.getName() ;
      return getFileSource(input, fileName, dservice);
    } catch (Exception e) {
      e.printStackTrace() ;
      return null;
    }
  }
  
  public void setQuestionView(String questionid){
    this.questionView_ = questionid ;
  }
  
  public String getCategoryId(){
    return this.categoryId_ ;
  }
  
  public void setCategoryId(String categoryId)  throws Exception {
    this.categoryId_ = categoryId ;
    this.isChangeLanguage = false ;
    setCategories() ;
  }
  
  public String getQuestionRelationById(String questionId) {
    Question question = new Question();
    try {
      question = faqService.getQuestionById(questionId, FAQUtils.getSystemProvider());
      if(question != null) {
        return question.getCategoryId() + "/" + question.getId() + "/" + question.getQuestion();
      } else {
        return "" ;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return "" ;
    }
  }
	
	public void moveDownUp(Event<UIQuestions> event, int i) {
  	String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
  	int index = 0 ;
  	for (Category cate : categories_) {
  		if (cate.getId().equals(categoryId)) {
  			break ;
  		} else {
  			index ++ ;
  		}
  	}

  	if (index < 0) return ;
  	if ( index ==0 && i == -1) return ;
  	if (index == categories_.size()-1 && i==1) return ;
  	Category category = categories_.remove(index) ;
  	for (Category cate : categories_) {
  	}
  	categories_.add(index+i, category) ;
  }	
  
  private String getBackPath() {
    return this.backPath_ ;
  }
	
  public List<String> getListEmail(String categoryId) throws Exception {
  	FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
    emailList_ = faqService.getListMailInWatch(categoryId, FAQUtils.getSystemProvider()) ;
    return emailList_ ;
  }
  
  public void setListEmail(List<String> list) { emailList_ = list ;}
  
  public void setPath(String s) { newPath_ = s ; }

  public String getPathService(String categoryId) throws Exception {
  	String oldPath = "";
		List<String> listPath = faqService.getCategoryPath(FAQUtils.getSystemProvider(), categoryId) ;
    for(int i = listPath.size() -1 ; i >= 0; i --) {
    	oldPath = oldPath + "/" + listPath.get(i);
    }
    String path = "FAQService"+ oldPath ;
    oldPath = path.substring(0, path.lastIndexOf("/")) ;
    return oldPath ;
  }
  
  public String cutCaret(String name) {
  	StringBuffer string = new StringBuffer();
    char c;
    for (int i = 0; i < name.length(); i++) {
     c = name.charAt(i) ;
     if(c == 47) continue ;
     string.append(c) ;
      }
    return string.toString();
  }
  
  private List<Category> getAllSubCategory(String categoryId) throws Exception {
    List<Category> listResult = new ArrayList<Category>() ;
    Stack<Category> stackCate = new Stack<Category>() ;
    SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
    Category cate = null ;
    listResult.add(faqService.getCategoryById(categoryId, sessionProvider)) ;
    for(Category category : faqService.getSubCategories(categoryId, sessionProvider, this.faqSetting_)) {
      stackCate.push(category) ;
    }
    while(!stackCate.isEmpty()) {
      cate = stackCate.pop() ;
      listResult.add(cate) ;
      for(Category category : faqService.getSubCategories(cate.getId(), sessionProvider, this.faqSetting_)) {
        stackCate.push(category) ;
      }
    }
    return listResult ;
  }
  
	static  public class AddCategoryActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
    	UIQuestions question = event.getSource() ; 
    	String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ; 
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
			UIPopupContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UICategoryForm category = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
      if(!FAQUtils.isFieldEmpty(categoryId)) {
      	try {
      		Category cate = faqService.getCategoryById(categoryId, FAQUtils.getSystemProvider()) ;
          String moderator[] = cate.getModeratorsCategory() ;
          String currentUser = FAQUtils.getCurrentUser() ;
          FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
          if(Arrays.asList(moderator).contains(currentUser)|| serviceUtils.isAdmin(currentUser)) {
          	uiPopupAction.activate(uiPopupContainer, 520, 300) ;
          	uiPopupContainer.setId("SubCategoryForm") ;
          	category.setParentId(categoryId) ;
          } else {
            uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
            question.setCategories() ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
            return ;
          }
        } catch (Exception e) {
          uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
          question.setCategories() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
          return ;
        }
      } else {
      	uiPopupAction.activate(uiPopupContainer, 520, 300) ;
      	uiPopupContainer.setId("AddCategoryForm") ;
      }
      category.init(true) ;
		  event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      UIFAQContainer fAQContainer = question.getAncestorOfType(UIFAQContainer.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer) ;
		}
	}
  
  static public class AddNewQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource() ;
      questions.isChangeLanguage = false ;
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
      try {
        faqService.getCategoryById(categoryId, FAQUtils.getSystemProvider());
      } catch (Exception e) {
        UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        try {
          questions.setCategories() ;
        } catch (Exception pathEx){
          UIFAQContainer container = questions.getParent() ;
          UIBreadcumbs breadcumbs = container.findFirstComponentOfType(UIBreadcumbs.class) ;
          String pathCate = "" ;
          for(String path : breadcumbs.paths_.get(breadcumbs.paths_.size() - 1).split("/")) {
            if(path.equals("FAQService")){
              pathCate = path ;
              continue ;
            }
            try {
              faqService.getCategoryById(path, FAQUtils.getSystemProvider());
              if(pathCate.trim().length() > 0) pathCate += "/" ;
              pathCate += path ;
            } catch (Exception pathExc) {
              try {
                breadcumbs.setUpdataPath(pathCate) ;
              } catch (Exception exc) {
                e.printStackTrace();
              }
              if(pathCate.indexOf("/") > 0) {
                questions.setCategoryId(pathCate.substring(pathCate.lastIndexOf("/") + 1)) ;
                questions.setListQuestion() ;
              } else {
                questions.categoryId_ = null ;
                questions.setCategories() ;
                questions.setListQuestion() ;
              }
              break ;
            }
          }
        }
        UIFAQContainer fAQContainer = questions.getAncestorOfType(UIFAQContainer.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer) ;
        return ;
      }
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
      UIQuestionForm questionForm = popupContainer.addChild(UIQuestionForm.class, null, null) ;
      ContactService contactService = questions.getApplicationComponent(ContactService.class) ;
      String email = "" ;
      String name = "" ;
      if(FAQUtils.getCurrentUser() != null){
	      Contact contact = contactService.getContact(FAQUtils.getSystemProvider()
	      		, FAQUtils.getCurrentUser(), FAQUtils.getCurrentUser()) ;
	      try {
	      	name = contact.getFullName();
	      } catch (NullPointerException e) {
	      	name = "" ;
	      }
	      try {
	      	email = contact.getEmailAddress() ;
	      } catch (NullPointerException e) {
	      	email = "" ;
	      }
      }
      questionForm.setAuthor(name) ;
      questionForm.setEmail(email) ;
      questionForm.setCategoryId(categoryId) ;
      questionForm.refresh() ;
      popupContainer.setId("AddQuestion") ;
      popupAction.activate(popupContainer, 600, 400) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class OpenCategoryActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource() ;
      questions.backPath_ = "" ;
      UIFAQPortlet faqPortlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      try {
        faqService.getCategoryById(categoryId, FAQUtils.getSystemProvider()) ;
      } catch (Exception e) {
        UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        questions.setCategories() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
        return ;
      }
      questions.setCategoryId(categoryId) ;
      questions.setListQuestion() ;
      UIBreadcumbs breadcumbs = faqPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;
      String oldPath = breadcumbs.getPaths() ;
      if(oldPath != null && oldPath.trim().length() > 0) {
      	if(!oldPath.contains(categoryId)) {
      		newPath_ = oldPath + "/" +categoryId ;
      		questions.setPath(newPath_) ;
      		breadcumbs.setUpdataPath(oldPath + "/" +categoryId);
      	}
      } else breadcumbs.setUpdataPath(categoryId);
			event.getRequestContext().addUIComponentToUpdateByAjax(breadcumbs) ;
      UIFAQContainer fAQContainer = questions.getAncestorOfType(UIFAQContainer.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer) ;
    }
  }
	
	static	public class EditCategoryActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
			try {
        Category cate = faqService.getCategoryById(categoryId, FAQUtils.getSystemProvider()) ;
        String moderator[] = cate.getModeratorsCategory() ;
        String currentUser = FAQUtils.getCurrentUser() ;
        FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
        if(Arrays.asList(moderator).contains(currentUser)|| serviceUtils.isAdmin(currentUser)) {
        	UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class,520) ;
    			uiPopupContainer.setId("EditCategoryForm") ;
          UICategoryForm uiCategoryForm = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
    			uiCategoryForm.init(false);
    			uiCategoryForm.setCategoryValue(categoryId, true) ;
    			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
        } else {
          uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
          question.setCategories() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
          return ;
        }
      } catch (Exception e) {
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        question.setCategories() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
        return ;
      }
		}
	}
	
	static	public class DeleteCategoryActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 			
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIApplication uiApp = question.getAncestorOfType(UIApplication.class) ;
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
			try {
				SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
				Category cate = faqService.getCategoryById(categoryId, sessionProvider) ;
        String moderator[] = cate.getModeratorsCategory() ;
        String currentUser = FAQUtils.getCurrentUser() ;
        FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
        if(Arrays.asList(moderator).contains(currentUser)|| serviceUtils.isAdmin(currentUser)) {
          List<Category> listCate = question.getAllSubCategory(categoryId) ;
          FAQSetting faqSetting = new FAQSetting();
          faqSetting.setDisplayMode(FAQUtils.DISPLAYBOTH);
        	for(Category category : listCate) {
          	String id = category.getId() ;
          	List<Question> listQuestion = faqService.getAllQuestionsByCatetory(id, FAQUtils.getSystemProvider(), faqSetting).getAll() ;
            for(Question ques: listQuestion) {
            	String questionId = ques.getId() ;
            	faqService.removeQuestion(questionId, FAQUtils.getSystemProvider()) ;
            }
          }
          faqService.removeCategory(categoryId, FAQUtils.getSystemProvider()) ;
    			question.setCategories() ;
    			event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
        } else {
          uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
          question.setCategories() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
          return ;
        }
      } catch (Exception e) {
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        question.setCategories() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
        return ;
      }
		}
	}
	
	static	public class MoveCategoryActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet faqPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = faqPortlet.getChild(UIPopupAction.class);
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
			try {
				Category cate = faqService.getCategoryById(categoryId, FAQUtils.getSystemProvider()) ;
        String moderator[] = cate.getModeratorsCategory() ;
        String currentUser = FAQUtils.getCurrentUser() ;
        FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
        if(Arrays.asList(moderator).contains(currentUser)|| serviceUtils.isAdmin(currentUser)) {
        	List<Category> listCate = faqService.getSubCategories(null, FAQUtils.getSystemProvider(), question.faqSetting_) ;
        	String cateId = null ;
        	if(listCate.size() == 1 ) {
		      	for(Category cat: listCate) { cateId = cat.getId(); }
		      } 
        	if(listCate.size() > 1 || listCate.size() == 1 && !categoryId.equals(cateId)) {
	        	UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
	    			UIMoveCategoryForm uiMoveCategoryForm = popupAction.activate(UIMoveCategoryForm.class, 600) ;
	    			popupContainer.setId("MoveCategoryForm") ;
	    			uiMoveCategoryForm.setCategoryID(categoryId) ;
	    			uiMoveCategoryForm.setFAQSetting(question.faqSetting_) ;
	    			uiMoveCategoryForm.setListCate() ;
	    			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
        	} else {
        		uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.cannot-move-category", null, ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
            question.setCategories() ;
            event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
            return ;
        	}
        } else {
          uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
          question.setCategories() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
          return ;
        }
      } catch (Exception e) {
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        question.setCategories() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
        return ;
      }
		}
	}
  
	static	public class MoveUpActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 
			UIFAQPortlet faqPortlet = question.getAncestorOfType(UIFAQPortlet.class) ;
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      try {
      	Category cate = faqService.getCategoryById(categoryId, FAQUtils.getSystemProvider()) ;
        String moderator[] = cate.getModeratorsCategory() ;
        String currentUser = FAQUtils.getCurrentUser() ;
        FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
        if(Arrays.asList(moderator).contains(currentUser)|| serviceUtils.isAdmin(currentUser)) {
        	question.moveDownUp(event, -1);
    			event.getRequestContext().addUIComponentToUpdateByAjax(question) ;
        } else {
          uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
          question.setCategories() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
          return ;
        }
      } catch (Exception e) {
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        question.setCategories() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
        return ;
      }
		}
  }
	
	static	public class MoveDownActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 
			UIFAQPortlet faqPortlet = question.getAncestorOfType(UIFAQPortlet.class) ;
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      try {
      	Category cate = faqService.getCategoryById(categoryId, FAQUtils.getSystemProvider()) ;
        String moderator[] = cate.getModeratorsCategory() ;
        String currentUser = FAQUtils.getCurrentUser() ;
        FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
        if(Arrays.asList(moderator).contains(currentUser)|| serviceUtils.isAdmin(currentUser)) {
        	question.moveDownUp(event, 1);
    			event.getRequestContext().addUIComponentToUpdateByAjax(question) ;
        } else {
          uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
          question.setCategories() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
          return ;
        }
      } catch (Exception e) {
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        question.setCategories() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
        return ;
      }
		}
	}
	
	static	public class SettingActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
    	UIQuestions question = event.getSource() ; 
    	String cateId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;	
			UISettingForm uiSetting = popupAction.activate(UISettingForm.class, 400) ;
			popupContainer.setId("CategorySettingForm") ;
			uiSetting.setFaqSetting(question.faqSetting_);
      uiSetting.init() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
  
	static	public class WatchActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
    	UIQuestions question = event.getSource() ;
    	String cateId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
      try {
        faqService.getCategoryById(cateId, FAQUtils.getSystemProvider()) ;
      } catch (Exception e) {
        UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        question.setCategories() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
        return ;
      }
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIWatchForm uiWatchForm = popupAction.activate(UIWatchForm.class, 420) ;
			popupContainer.setId("CategoryWatchForm") ;
			uiWatchForm.setCategoryID(cateId) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static	public class WatchManagerActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
    	UIQuestions question = event.getSource() ;
    	String cateId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet faqPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = faqPortlet.getChild(UIPopupAction.class);
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
			try {
				Category cate = faqService.getCategoryById(cateId, FAQUtils.getSystemProvider()) ;
        String moderator[] = cate.getModeratorsCategory() ;
        String currentUser = FAQUtils.getCurrentUser() ;
        FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
        if(Arrays.asList(moderator).contains(currentUser)|| serviceUtils.isAdmin(currentUser)) {
        	UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
    			UIWatchContainer watchContainer = popupAction.activate(UIWatchContainer.class, 800) ;
    			UIWatchManager watchManager = watchContainer.getChild(UIWatchManager.class) ;
    			popupContainer.setId("WatchManager") ;
    			watchManager.setCategoryID(cateId) ;
    			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
        } else {
          uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
          question.setCategories() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
          return ;
        }
      } catch (Exception e) {
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        question.setCategories() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
        return ;
      }
		}
	}

	static  public class EditSubCategoryActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
    	UIQuestions questions = event.getSource() ; 
    	String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet faqPortlet = questions.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction uiPopupAction = faqPortlet.getChild(UIPopupAction.class) ; 
			UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
			try {
				String newPath = questions.cutCaret(newPath_) ;
				String pathService = questions.cutCaret(questions.getPathService(categoryId)) ;
				Category cate = faqService.getCategoryById(categoryId, FAQUtils.getSystemProvider()) ;
        String moderator[] = cate.getModeratorsCategory() ;
        String currentUser = FAQUtils.getCurrentUser() ;
        FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
        if(Arrays.asList(moderator).contains(currentUser) || serviceUtils.isAdmin(currentUser)) {
        	if (newPath.equals(pathService)) {
		      	UIPopupContainer uiPopupContainer = uiPopupAction.activate(UIPopupContainer.class,520) ;  
		        uiPopupContainer.setId("EditSubCategoryForm") ;
		  		  UICategoryForm categoryForm = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
		  		  categoryForm.init(false) ;
		  		  String parentCategoryId = newPath_.substring(newPath_.lastIndexOf("/")+1, newPath_.length()) ;
		  		  categoryForm.setParentId(parentCategoryId) ;
		  		  categoryForm.setCategoryValue(categoryId, true) ;
		  		  event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
        	} else {
        		uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-moved-action", null, ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
            questions.setCategories() ;
            event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
            return ;
        	}
        } else {
          uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
          questions.setCategories() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
          return ;
        }
      } catch (Exception e) {
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        questions.setCategories() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
        return ;
      }
		}
	}
  
  // action for question :
  
  static  public class QuestionManagamentActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource() ;
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;

      UIQuestionManagerForm questionManagerForm = popupContainer.addChild(UIQuestionManagerForm.class, null, null) ;
      popupContainer.setId("FAQQuestionManagerment") ;
      popupAction.activate(popupContainer, 900, 850) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
	static  public class ViewQuestionActionListener extends EventListener<UIQuestions> {
	  public void execute(Event<UIQuestions> event) throws Exception {
	    UIQuestions uiQuestions = event.getSource() ; 
      uiQuestions.isChangeLanguage = false ;
      String strId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String questionId = new String() ;
      language_ = "" ;
      Question question = new Question();
      try{
        if(strId.indexOf("/") < 0) {
          questionId = strId ;
          question = faqService.getQuestionById(questionId, FAQUtils.getSystemProvider()) ;
          uiQuestions.backPath_ = "" ;
          for(int i = 0; i < uiQuestions.listQuestion_.size(); i ++) {
            if(uiQuestions.listQuestion_.get(i).getId().equals(uiQuestions.questionView_)) {
              uiQuestions.listQuestion_.get(i).setQuestion(uiQuestions.listQuestionLanguage.get(0).getQuestion()) ;
              uiQuestions.listQuestion_.get(i).setLanguage(uiQuestions.listQuestionLanguage.get(0).getLanguage()) ;
              uiQuestions.listQuestion_.get(i).setResponses(uiQuestions.listQuestionLanguage.get(0).getResponse()) ;
              uiQuestions.listQuestion_.get(i).setDateResponse(uiQuestions.listQuestionLanguage.get(0).getDateResponse());
              break ;
            }
          }
        } else {
          if(uiQuestions.backPath_ != null && uiQuestions.backPath_.trim().length() > 0 && uiQuestions.backPath_.equals(strId)) {
            uiQuestions.backPath_ = "" ;
          } else {
            uiQuestions.backPath_ = uiQuestions.categoryId_ + "/" + uiQuestions.questionView_ ;
          }
          questionId = strId.split("/")[1] ;
          question = faqService.getQuestionById(questionId, FAQUtils.getSystemProvider()) ;
          String categoryId = question.getCategoryId();
          UIFAQPortlet faqPortlet = uiQuestions.getAncestorOfType(UIFAQPortlet.class) ;
          uiQuestions.setCategoryId(categoryId) ;
          uiQuestions.setListQuestion() ;
          uiQuestions.listCateId_.clear() ;
          uiQuestions.setIsModerators() ;
          UIBreadcumbs breadcumbs = faqPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;
          breadcumbs.setUpdataPath(null) ;
          String oldPath = "" ;
          FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
          List<String> listPath = faqService.getCategoryPath(FAQUtils.getSystemProvider(), categoryId) ;
          for(int i = listPath.size() -1 ; i >= 0; i --) {
            oldPath = oldPath + "/" + listPath.get(i);
          } 
          newPath_ = "FAQService"+oldPath ;
          breadcumbs.setUpdataPath(newPath_);
          event.getRequestContext().addUIComponentToUpdateByAjax(breadcumbs) ;
          UIFAQContainer fAQContainer = uiQuestions.getAncestorOfType(UIFAQContainer.class) ;
        }
        
        List<String> listRelaId = new ArrayList<String>() ;
        for(String quesRelaId : question.getRelations()) {
          try {
            faqService.getQuestionById(quesRelaId, FAQUtils.getSystemProvider()) ;
            listRelaId.add(quesRelaId) ;
          } catch (Exception e) { }
        }
        if(listRelaId.size() < question.getRelations().length) {
          question.setRelations(listRelaId.toArray(new String[]{})) ;
          faqService.saveQuestion(question, false, FAQUtils.getSystemProvider()) ;
          for(int i = 0 ; i < uiQuestions.getListQuestion().size() ; i ++) {
            if(uiQuestions.getListQuestion().get(i).getId().equals(questionId)) {
              uiQuestions.getListQuestion().set(i, question) ;
              break ;
            }
          }
        }
        if( uiQuestions.questionView_ == null || uiQuestions.questionView_.trim().length() < 1 || !uiQuestions.questionView_.equals(questionId)){
          uiQuestions.questionView_ = questionId ; 
        } else {
          uiQuestions.isChangeLanguage = true ;
          uiQuestions.questionView_ = "" ;
        }
      } catch(javax.jcr.PathNotFoundException e) {
        UIFAQPortlet faqPortlet = uiQuestions.getAncestorOfType(UIFAQPortlet.class) ;
        UIApplication uiApplication = uiQuestions.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        uiQuestions.setListQuestion() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
        return ;
      } catch (Exception e) { 
        e.printStackTrace() ;
      }
      UIFAQContainer fAQContainer = uiQuestions.getAncestorOfType(UIFAQContainer.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer) ;
	  }
	}
  
  static  public class ResponseQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions question = event.getSource() ; 
      UIFAQPortlet portlet = question.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
      Question question2 = null ;
      try{
        question2 = faqService.getQuestionById(event.getRequestContext().getRequestParameter(OBJECTID), FAQUtils.getSystemProvider());
      } catch(javax.jcr.PathNotFoundException e) {
        UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        question.setListQuestion() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
        return ;
      } catch (Exception e) { 
        e.printStackTrace() ;
      } 
      UIResponseForm responseForm = popupContainer.addChild(UIResponseForm.class, null, null) ;
      responseForm.setQuestionId(question2, language_) ;
      popupContainer.setId("FAQResponseQuestion") ;
      popupAction.activate(popupContainer, 720, 1000) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static  public class EditQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource() ;
      questions.isChangeLanguage = false ;
      String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
      Question question = null ;
      try{
        question = faqService.getQuestionById(questionId, FAQUtils.getSystemProvider()) ;
      } catch(javax.jcr.PathNotFoundException e) {
        UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        questions.setListQuestion() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
        return ;
      } catch (Exception e) { 
        e.printStackTrace() ;
      } 
      UIQuestionForm questionForm = popupContainer.addChild(UIQuestionForm.class, null, null) ;
      questionForm.setQuestionId(question) ;
      popupContainer.setId("EditQuestion") ;
      popupAction.activate(popupContainer, 600, 450) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static  public class DeleteQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource() ; 
      String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
      Question question = null ;
      try{
        question = faqService.getQuestionById(questionId, FAQUtils.getSystemProvider()) ;
      } catch (javax.jcr.PathNotFoundException e) {
        e.printStackTrace() ;
        UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        questions.setListQuestion() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
        return ;
      } catch (Exception e) { 
        e.printStackTrace() ;
      } 
      UIDeleteQuestion deleteQuestion = popupContainer.addChild(UIDeleteQuestion.class, null, null) ;
      deleteQuestion.setQuestionId(question) ;
      popupContainer.setId("FAQDeleteQuestion") ;
      popupAction.activate(popupContainer, 450, 400) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static  public class MoveQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource() ; 
      String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
      try {
        faqService.getQuestionById(questionId, FAQUtils.getSystemProvider()) ;
      } catch (javax.jcr.PathNotFoundException e) {
        UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        questions.setListQuestion() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
        return ;
      } catch (Exception e) { 
        e.printStackTrace() ;
      } 
      UIMoveQuestionForm moveQuestionForm = popupContainer.addChild(UIMoveQuestionForm.class, null, null) ;
      moveQuestionForm.setQuestionId(questionId) ;
      popupContainer.setId("FAQMoveQuestion") ;
      popupAction.activate(popupContainer, 500, 150) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static  public class SendQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions uiQuestions = event.getSource() ; 
      String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIFAQPortlet portlet = uiQuestions.getAncestorOfType(UIFAQPortlet.class) ;
      Question question = null ;
      try{
        question = faqService.getQuestionById(questionId, FAQUtils.getSystemProvider()) ;
      } catch (Exception e) {
        UIApplication uiApplication = uiQuestions.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        uiQuestions.setListQuestion() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
        return ;
      }
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
      UISendMailForm sendMailForm = popupContainer.addChild(UISendMailForm.class, null, null) ;
      if(!questionId.equals(uiQuestions.questionView_) || FAQUtils.isFieldEmpty(language_)) sendMailForm.setUpdateQuestion(questionId , "") ;
      else sendMailForm.setUpdateQuestion(questionId , language_) ;
      popupContainer.setId("FAQSendMailForm") ;
      popupAction.activate(popupContainer, 700, 1000) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static  public class ChangeQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions uiQuestions = event.getSource() ; 
      String[] stringInput = event.getRequestContext().getRequestParameter(OBJECTID).split("/") ;
      int pos = Integer.parseInt(stringInput[0]) ;
      language_ = stringInput[1] ;
      for(QuestionLanguage questionLanguage : uiQuestions.listQuestionLanguage) {
        if(questionLanguage.getLanguage().equals(language_)) {
          uiQuestions.listQuestion_.get(pos).setQuestion(questionLanguage.getQuestion());
          uiQuestions.listQuestion_.get(pos).setLanguage(questionLanguage.getLanguage());
          uiQuestions.listQuestion_.get(pos).setResponses(questionLanguage.getResponse());
          uiQuestions.listQuestion_.get(pos).setResponseBy(questionLanguage.getResponseBy());
          uiQuestions.listQuestion_.get(pos).setDateResponse(questionLanguage.getDateResponse());
          break ;
        }
      }
      uiQuestions.isChangeLanguage = true ;
      UIFAQContainer fAQContainer = uiQuestions.getAncestorOfType(UIFAQContainer.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer) ;
    }
  }

}