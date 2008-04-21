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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.webui.popup.UICategoryForm;
import org.exoplatform.faq.webui.popup.UIMoveCategoryForm;
import org.exoplatform.faq.webui.popup.UIPopupAction;
import org.exoplatform.faq.webui.popup.UIPopupContainer;
import org.exoplatform.faq.webui.popup.UISettingForm;
import org.exoplatform.faq.webui.popup.UIWatchForm;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
		template =	"app:/templates/faq/webui/UIQuestions.gtmpl" ,
		events = {
			@EventConfig(listeners = UIQuestions.AddCategoryActionListener.class),
			@EventConfig(listeners = UIQuestions.SubCategoryActionListener.class),
	    @EventConfig(listeners = UIQuestions.AddQuestionActionListener.class),
	    @EventConfig(listeners = UIQuestions.OpenCategoryActionListener.class),
	    
	    @EventConfig(listeners = UIQuestions.EditCategoryActionListener.class),
	    @EventConfig(listeners = UIQuestions.DeleteCategoryActionListener.class),
	    @EventConfig(listeners = UIQuestions.MoveCategoryActionListener.class),
	    @EventConfig(listeners = UIQuestions.MoveDownActionListener.class),
	    @EventConfig(listeners = UIQuestions.MoveUpActionListener.class),
	    @EventConfig(listeners = UIQuestions.CategorySettingActionListener.class),
	    @EventConfig(listeners = UIQuestions.WatchActionListener.class)
		}
)
public class UIQuestions extends UIContainer {
	private List<Category> categories_ = null ;
	private boolean isUpdate = true;
	private boolean	isEditCategory = false ;
	private	FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
	public UIQuestions()throws Exception {
		setCategories() ;
	}
  
  @SuppressWarnings("unused")
  private String[] getActionCategory(){
    String[] action = new String[]{"SubCategory", "EditCategory", "DeleteCategory", "MoveCategory", "MoveDown", "MoveUp", "CategorySetting", "Watch"} ;
    return action ;
  }
  
  public void setCategories() throws Exception  {
  	FAQService faqService =	(FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
		categories_ = faqService.getAllCategories(FAQUtils.getSystemProvider());
  }
	
	@SuppressWarnings("unused")
  private List<Category> getCategories() throws Exception {
		return categories_ ;
	}
	
	public void moveDownUp(Event<UIQuestions> event, int i) {
  	String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
  	int index = 0 ;
  	for (Category c : categories_) {
  		if (c.getId().equals(categoryId)) {
  			break ;
  		} else {
  			index ++ ;
  		}
  	}

  	if(index < 0) return ;
  	if( index ==0 && i == -1) return ;
  	if (index == 1 && i==1 ) return ;
  	Category category = categories_.remove(index) ;
  	for (Category cate : categories_) {
  		System.out.println(" cate sau remove : " + cate) ;
  	}
  	categories_.add(index+i, category) ;
  	for (Category cate : categories_) {
  		System.out.println(" cate sau add : " + cate) ;
  	}
  }	
	
	static  public class AddCategoryActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
    	System.out.println("\n\n AddCategoryActionListener");
    	UIQuestions question = event.getSource() ; 
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ; 
      UIPopupContainer uiPopupContainer = uiPopupAction.activate(UIPopupContainer.class,520) ;  
		  uiPopupContainer.setId("AddCategoryForm") ;
		  UICategoryForm category = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
		  category.init() ;
		  event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
		}
	}
  
  static public class AddQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
//      System.out.println("========> AddQuestionActionListener") ;
//      UIQuestions questions = event.getSource() ;
//      UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
//      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
//      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
//      @SuppressWarnings("unused")
//      //UIQuestionForm questionForm = popupContainer.addChild(UIQuestionForm.class, null, null) ;
//      UIResponseForm questionForm = popupContainer.addChild(UIResponseForm.class, null, null) ;
//      popupContainer.setId("AddQuestion") ;
//      popupAction.activate(popupContainer, 650, 950) ;
//      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class OpenCategoryActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      System.out.println("========> AddQuestionActionListener") ;
      UIQuestions questions = event.getSource() ;
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      System.out.println("\n\n\n\n>>>> categoryId : " + categoryId);
    }
  }
  
	
	static	public class EditCategoryActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			System.out.println("\n\n EditCategoryActionListener");
			UIQuestions question = event.getSource() ; 
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class,520) ;
			uiPopupContainer.setId("EditCategoryForm") ;
      UICategoryForm uiCategoryForm = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
			uiCategoryForm.init();
			uiCategoryForm.setCategoryValue(categoryId, true) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			question.isEditCategory = true ;
			}
		}
	
	static	public class DeleteCategoryActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			System.out.println("\n\n DeleteCategoryActionListener");
			UIQuestions uiQuestion = event.getSource() ; 			
			String CategoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = uiQuestion.getAncestorOfType(UIFAQPortlet.class);
			if(CategoryId != null) {
				uiQuestion.faqService.removeCategory(CategoryId, FAQUtils.getSessionProvider()) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
		}
	}
	
	static	public class MoveCategoryActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			System.out.println("\n\n MoveCategoryActionListener");
			UIQuestions question = event.getSource() ; 
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			popupContainer.addChild(UIMoveCategoryForm.class, null, null) ;
			popupContainer.setId("MoveCategoryForm") ;
			popupAction.activate(popupContainer, 410, 200) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			}
		}
	static	public class MoveUpActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 
			question.moveDownUp(event, -1);
			event.getRequestContext().addUIComponentToUpdateByAjax(question) ;
		}
  }
	static	public class MoveDownActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 
			question.moveDownUp(event, 1);
			event.getRequestContext().addUIComponentToUpdateByAjax(question) ;
		}
		}
	static	public class CategorySettingActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			System.out.println("\n\n CategorySettingActionListener");
    	UIQuestions question = event.getSource() ; 
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;	
			UISettingForm uiSetting = popupAction.activate(UISettingForm.class, 400) ;
			popupContainer.setId("CategorySettingForm") ;
      uiSetting.init() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			}
		}
	static	public class WatchActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			System.out.println("\n\n WatchActionListener");
    	UIQuestions question = event.getSource() ; 
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			popupContainer.addChild(UIWatchForm.class, null, null) ;
			popupContainer.setId("CategoryWatchForm") ;
			popupAction.activate(popupContainer, 410, 200) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			}
		}
	
	static  public class SubCategoryActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
    	System.out.println("\n\n SubCategoryActionListener");
    	UIQuestions question = event.getSource() ; 
    	String CategoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ; 
      UIPopupContainer uiPopupContainer = uiPopupAction.activate(UIPopupContainer.class,520) ;  
		  uiPopupContainer.setId("SubCategoryForm") ;
		  UICategoryForm category = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
		  category.setParentPath(CategoryId) ;
		  System.out.println("=====>>>>>>CategoryId: ") ;
		  category.init() ;
		  event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
		}
	}
	
}