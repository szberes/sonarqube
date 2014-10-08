/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.sonar.server.batch;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.Response;
import org.sonar.server.computation.ComputationService;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class UploadReportActionTest {

  private static final String DEFAULT_PROJECT_KEY = "123456789-987654321";
  private UploadReportAction sut;

  private ComputationService computationService;

  @Before
  public void before() {
    computationService = mock(ComputationService.class);

    sut = new UploadReportAction(computationService);
  }

  @Test
  public void verify_that_computation_service_is_called() throws Exception {
    Response response = mock(Response.class);
    Request request = mock(Request.class);
    when(request.mandatoryParam(anyString())).thenReturn(DEFAULT_PROJECT_KEY);

    sut.handle(request, response);

    verify(computationService).create(DEFAULT_PROJECT_KEY);
  }

}