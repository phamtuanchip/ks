/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.wiki.commons;

import java.util.StringTokenizer;

import org.exoplatform.services.jcr.datamodel.IllegalNameException;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 28 Mar 2011  
 */
public class NameValidator {
  
  public static void validate(String s) throws Exception {
    String invalidCharacters = ": @ / \\ | ^ # ; [ ] { } < > * ' \" + ? &"; // and .
    StringTokenizer tokens;
    if (s == null || s.trim().length() == 0) {
      throw new IllegalNameException();
    }
    for (int i = 0; i < s.length(); i++) {
      tokens = new StringTokenizer(invalidCharacters);
      char c = s.charAt(i);
      boolean isInvalid = false;
      while (tokens.hasMoreTokens()) {
        String test = tokens.nextToken();
        isInvalid = test.equals(String.valueOf(c));
        if (isInvalid == true)
          break;
      }
      if (Character.isLetter(c) || Character.isDigit(c) || (!isInvalid)) {
        continue;
      } else {
        throw new IllegalNameException(invalidCharacters);
      }
    }     
  }  
}
