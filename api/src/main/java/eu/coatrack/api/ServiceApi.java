package eu.coatrack.api;

/*-
 * #%L
 * coatrack-api
 * %%
 * Copyright (C) 2013 - 2020 Corizon | Institut für angewandte Systemtechnik Bremen GmbH (ATB)
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author gr-hovest
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "service_apis")
public class ServiceApi implements ServiceApiInterface{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @Size(max=1000)
    private String description;

    @Column(nullable = false)
    private String uriIdentifier;

    @Column(nullable = false)
    private String localUrl;


    @Enumerated(EnumType.STRING)
    private ServiceAccessPermissionPolicy serviceAccessPermissionPolicy;

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<ApiKey> apiKeys = new ArrayList<>(0);

    @Enumerated(EnumType.STRING)
    private ServiceAccessPaymentPolicy serviceAccessPaymentPolicy;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("position")
    private List<EntryPoint> entryPoints = new ArrayList<>(0);

    @OneToOne
    @JsonIgnore
    private User owner;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date deletedWhen;

    private double monthlyFee;

    @Override
    public String getServiceOwnerUsername() {
        return owner.getUsername();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 37 * hash + Objects.hashCode(this.name);
        hash = 37 * hash + Objects.hashCode(this.description);
        hash = 37 * hash + Objects.hashCode(this.uriIdentifier);
        hash = 37 * hash + Objects.hashCode(this.localUrl);
        hash = 37 * hash + Objects.hashCode(this.serviceAccessPermissionPolicy);
        hash = 37 * hash + Objects.hashCode(this.apiKeys);
        hash = 37 * hash + Objects.hashCode(this.serviceAccessPaymentPolicy);
        hash = 37 * hash + Objects.hashCode(this.entryPoints);
        hash = 37 * hash + Objects.hashCode(this.owner);
        hash = 37 * hash + Objects.hashCode(this.deletedWhen);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ServiceApi other = (ServiceApi) obj;
        return this.id == other.id;
    }

    @Override
    public String toString() {
        return "ServiceApi{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", uriIdentifier='" + uriIdentifier + '\''
                + ", localUrl='" + localUrl + '\''
                + ", user=" + owner
                + '}';
    }
}
