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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.faq.webui;

import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.webui.popup.UISettingForm;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupMessages;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.form.UIFormInputInfo;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com 
 * Mar 04, 2008
 */

@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class, 
    template = "app:/templates/faq/webui/UIAnswersPortlet.gtmpl"
)
public class UIAnswersPortlet extends UIPortletApplication {
  private boolean         isFirstTime = true;

  /**
   * ui component for displaying message when changing mode.
   */
  private UIFormInputInfo changeModeMessage;

  public UIAnswersPortlet() throws Exception {
    changeModeMessage = new UIFormInputInfo("UIMessageEditMode", "UIMessageEditMode", "");
    changeModeMessage.setRendered(false);
    addChild(changeModeMessage);
    addChild(UIAnswersContainer.class, null, null);
    UIPopupAction uiPopup = addChild(UIPopupAction.class, null, null);
    uiPopup.setId("UIAnswersPopupAction");
    uiPopup.getChild(UIPopupWindow.class).setId("UIAnswersPopupWindow");
  }

  public String getSpaceCategoryId() {

    try {
      PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
      PortletPreferences pref = pcontext.getRequest().getPreferences();
      if (pref.getValue("SPACE_URL", null) != null) {
        String url = pref.getValue("SPACE_URL", null);
        SpaceService sService = (SpaceService) PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class);
        Space space = sService.getSpaceByUrl(url);
        String categoryId = Utils.CATE_SPACE_ID_PREFIX + space.getId();
        FAQService fService = (FAQService) PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class);
        if (fService.getCategoryById(categoryId) != null)
          return categoryId;
      }
      return null;
    } catch (Exception e) {
      return null;
    }

  }

  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    PortletRequestContext portletReqContext = (PortletRequestContext) context;
    if (portletReqContext.getApplicationMode() == PortletMode.VIEW) {
      changeModeMessage.setRendered(false);
      isFirstTime = true;
      if (getChild(UIAnswersContainer.class) == null) {
        if (getChild(UISettingForm.class) != null) {
          removeChild(UISettingForm.class);
        }
        addChild(UIAnswersContainer.class, null, null);
      }
      renderPortletById();
    } else if (portletReqContext.getApplicationMode() == PortletMode.EDIT) {
      try {
        changeModeMessage.setValue(context.getApplicationResourceBundle().getString("UIAnswersPortlet.label.deny-access-edit-mode"));
        if (isFirstTime) {
          isFirstTime = false;
          UIQuestions questions = getChild(UIAnswersContainer.class).getChild(UIQuestions.class);
          FAQSetting faqSetting = questions.getFAQSetting();
          removeChild(UIAnswersContainer.class);
          if (getChild(UISettingForm.class) == null) {
            if (faqSetting.isAdmin()) {
              UISettingForm settingForm = addChild(UISettingForm.class, null, "FAQPortletSetting");
              settingForm.setRendered(true);
              settingForm.setIsEditPortlet(true);
              settingForm.init();
            } else {
              changeModeMessage.setRendered(true);
            }

          }
        }
      } catch (Exception e) {
        log.error("\nFail to render a WebUIApplication\n", e);
      }
    }

    super.processRender(app, context);
  }

  private void renderPortletById() throws Exception {
    try {
      String cateId = getSpaceCategoryId();
      PortalRequestContext context = Util.getPortalRequestContext();
      if (!FAQUtils.isFieldEmpty(cateId) && context.getRequestParameter(OBJECTID) == null && !("true".equals("" + context.getRequestParameter("ajaxRequest")))) {
        UIBreadcumbs uiBreadcums = findFirstComponentOfType(UIBreadcumbs.class);
        UIQuestions uiQuestions = findFirstComponentOfType(UIQuestions.class);
        UICategories categories = findFirstComponentOfType(UICategories.class);
        uiBreadcums.setUpdataPath(Utils.CATEGORY_HOME + "/" + cateId);
        uiBreadcums.setRenderSearch(true);
        uiQuestions.setCategoryId(Utils.CATEGORY_HOME + "/" + cateId);
        categories.setPathCategory(Utils.CATEGORY_HOME + "/" + cateId);
      }
    } catch (Exception e) {
      log.error("can not render the selected category", e);
    }
  }

  public void renderPopupMessages() throws Exception {
    UIPopupMessages popupMess = getUIPopupMessages();
    if (popupMess == null)
      return;
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    popupMess.processRender(context);
  }

  public void cancelAction() throws Exception {
    WebuiRequestContext context = RequestContext.getCurrentInstance();
    UIPopupAction popupAction = getChild(UIPopupAction.class);
    popupAction.deActivate();
    context.addUIComponentToUpdateByAjax(popupAction);
  }

  public static String getPreferenceDisplay() {
    PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPref = pcontext.getRequest().getPreferences();
    String repository = portletPref.getValue("display", "");
    return repository;
  }
}
