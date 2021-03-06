/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
 */
package org.exoplatform.wiki.webui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTVersion;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.webui.control.action.RestoreActionListener;
import org.exoplatform.wiki.webui.control.action.ViewRevisionActionListener;
import org.exoplatform.wiki.webui.core.UIWikiForm;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 13, 2010  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiPageVersionsList.gtmpl",
  events = {
    @EventConfig(listeners = RestoreActionListener.class),
    @EventConfig(listeners = ViewRevisionActionListener.class),
    @EventConfig(listeners = UIWikiPageVersionsList.CompareRevisionActionListener.class)
  }
)
public class UIWikiPageVersionsList extends UIWikiForm {
  
  private List<NTVersion>    versionsList;

  public static final String RESTORE_ACTION = "Restore";

  public static final String VIEW_REVISION  = "ViewRevision";
  
  public static final String COMPARE_ACTION = "CompareRevision";

  public UIWikiPageVersionsList() throws Exception {
    super();
    this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.SHOWHISTORY, WikiMode.VIEW });    
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    this.versionsList = Utils.getCurrentPageRevisions();
    getChildren().clear();
    for (NTVersion version : this.versionsList) {
      addUIFormInput(new UIFormCheckBoxInput<Boolean>(version.getName(), "", false));
    }
    super.processRender(context);
  }

  public List<NTVersion> getVersionsList() throws Exception {
    return versionsList;
  }
  
  static public class CompareRevisionActionListener
                                                   extends
                                                   org.exoplatform.wiki.webui.control.action.CompareRevisionActionListener {
    @Override
    public void execute(Event<UIComponent> event) throws Exception {
      UIWikiPageVersionsList uiForm = (UIWikiPageVersionsList) event.getSource();
      List<NTVersion> checkedVersions = new ArrayList<NTVersion>();
      UIWikiPortlet wikiPortlet = uiForm.getAncestorOfType(UIWikiPortlet.class);
      List<NTVersion> versions = uiForm.versionsList;
      for (NTVersion version : versions) {
        UIFormCheckBoxInput<Boolean> uiCheckBox = uiForm.getChildById(version.getName());
        if (uiCheckBox.isChecked()) {
          checkedVersions.add(version);
        }
      }
      if (checkedVersions.size() != 2) {
        wikiPortlet.addMessage(new ApplicationMessage("UIWikiPageVersionsList.msg.checkGroup-required",
                                                null,
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(wikiPortlet.getUIPopupMessages());
        return;
      } else {
        this.setVersionToCompare(new ArrayList<NTVersion>(uiForm.versionsList));
        String fromVersionName = checkedVersions.get(0).getName();
        String toVersionName = checkedVersions.get(1).getName();
        for (int i = 0; i < versions.size(); i++) {
          NTVersion version = versions.get(i);
          if (version.getName().equals(fromVersionName)) {
            this.setFrom(i);
          }
          if (version.getName().equals(toVersionName)) {
            this.setTo(i);
          }
        }
        super.execute(event);
      }
    }
  }

}
