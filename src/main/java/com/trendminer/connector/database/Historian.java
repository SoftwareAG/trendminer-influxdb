package com.trendminer.connector.database;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;

@Entity
public class Historian {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    @NotEmpty
    private String name;
    private String prefix;
    @NotEmpty
    private String provider;
    private String dataSource;
    private String userId;
    private String password;
    private String version;

    public Historian() {
    }

    public Historian(
            @NotEmpty String name,
            String prefix,
            @NotEmpty String provider,
            String dataSource,
            String userId,
            String password,
            String version) {
        this.name = name;
        this.prefix = prefix;
        this.provider = provider;
        this.dataSource = dataSource;
        this.userId = userId;
        this.password = password;
        this.version = version;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getProvider() {
        return provider;
    }

    public String getDataSource() {
        return dataSource;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getVersion() {
        return version;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Historian)) {
            return false;
        }

        Historian historian = (Historian) o;

        return new EqualsBuilder()
                .append(id, historian.id)
                .append(name, historian.name)
                .append(prefix, historian.prefix)
                .append(provider, historian.provider)
                .append(dataSource, historian.dataSource)
                .append(userId, historian.userId)
                .append(password, historian.password)
                .append(version, historian.version)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(name)
                .append(prefix)
                .append(provider)
                .append(dataSource)
                .append(userId)
                .append(password)
                .append(version)
                .toHashCode();
    }
}
