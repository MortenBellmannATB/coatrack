/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.coatrack.admin.server.principal;

/*-
 * #%L
 * coatrack-config-server
 * %%
 * Copyright (C) 2013 - 2021 Corizon | Institut für angewandte Systemtechnik Bremen GmbH (ATB)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

/**
 *
 * @author perezdf
 */
public class UserPrincipal extends AbstractYggPrincipal {

    public UserPrincipal(String user, String password) {
        super(user, password);

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        GrantedAuthority grant = new GrantedAuthority() {
            @Override
            public String getAuthority() {

                return "ROLE_USER";
            }
        };
        Collection<GrantedAuthority> grantedCollection = new ArrayList<>();
        grantedCollection.add(grant);
        return grantedCollection;
    }

}
