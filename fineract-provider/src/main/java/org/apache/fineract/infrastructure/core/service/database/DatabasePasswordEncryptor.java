/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.core.service.database;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.security.service.PasswordEncryptor;
import org.apache.fineract.infrastructure.security.utils.EncryptionUtil;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabasePasswordEncryptor implements PasswordEncryptor {

    public static final String DEFAULT_ENCRYPTION = "AES/CBC/PKCS5Padding";

    public static final String DEFAULT_MASTER_PASSWORD = "fineract";

    private final FineractProperties fineractProperties;

    @Override
    public String encrypt(String plainPassword) {
        String masterPassword = Optional.ofNullable(fineractProperties.getTenant())
                .map(FineractProperties.FineractTenantProperties::getMasterPassword).orElse(DEFAULT_MASTER_PASSWORD);
        String encryption = Optional.ofNullable(fineractProperties.getTenant())
                .map(FineractProperties.FineractTenantProperties::getEncryption).orElse(DEFAULT_ENCRYPTION);
        return EncryptionUtil.encryptToBase64(encryption, masterPassword, plainPassword);
    }

    @Override
    public String decrypt(String encryptedPassword) {
        String masterPassword = Optional.ofNullable(fineractProperties.getTenant())
                .map(FineractProperties.FineractTenantProperties::getMasterPassword).orElse(DEFAULT_MASTER_PASSWORD);
        String encryption = Optional.ofNullable(fineractProperties.getTenant())
                .map(FineractProperties.FineractTenantProperties::getEncryption).orElse(DEFAULT_ENCRYPTION);
        return EncryptionUtil.decryptFromBase64(encryption, masterPassword, encryptedPassword);
    }

    public String getMasterPasswordHash() {
        String masterPassword = Optional.ofNullable(fineractProperties) //
                .map(FineractProperties::getTenant) //
                .map(FineractProperties.FineractTenantProperties::getMasterPassword) //
                .orElse(DEFAULT_MASTER_PASSWORD);
        return BCrypt.hashpw(masterPassword.getBytes(StandardCharsets.UTF_8), BCrypt.gensalt());
    }

    public boolean isMasterPasswordHashValid(String hashed) {
        String masterPassword = Optional.ofNullable(fineractProperties) //
                .map(FineractProperties::getTenant) //
                .map(FineractProperties.FineractTenantProperties::getMasterPassword) //
                .orElse(DEFAULT_MASTER_PASSWORD);
        return BCrypt.checkpw(masterPassword, hashed);
    }
}
