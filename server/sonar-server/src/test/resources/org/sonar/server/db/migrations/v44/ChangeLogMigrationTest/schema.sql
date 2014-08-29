
CREATE TABLE "RULES_PARAMETERS" (
  "ID" INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "RULE_ID" INTEGER NOT NULL,
  "NAME" VARCHAR(128) NOT NULL,
  "PARAM_TYPE" VARCHAR(512) NOT NULL,
  "DEFAULT_VALUE" VARCHAR(4000),
  "DESCRIPTION" VARCHAR(4000)
  );

  CREATE TABLE "RULES_PROFILES" (
  "ID" INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "NAME" VARCHAR(100) NOT NULL,
  "LANGUAGE" VARCHAR(20),
  "KEE" VARCHAR(255) NOT NULL,
  "PARENT_KEE" VARCHAR(255),
  "RULES_UPDATED_AT" VARCHAR(100),
  "CREATED_AT" TIMESTAMP,
  "UPDATED_AT" TIMESTAMP
  );

  CREATE TABLE "ACTIVE_RULE_PARAM_CHANGES" (
  "ID" INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "ACTIVE_RULE_CHANGE_ID" INTEGER NOT NULL,
  "RULES_PARAMETER_ID" INTEGER NOT NULL,
  "OLD_VALUE" VARCHAR(4000),
  "NEW_VALUE" VARCHAR(4000)
  );

  CREATE TABLE "RULES" (
  "ID" INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "PLUGIN_RULE_KEY" VARCHAR(200) NOT NULL,
  "PLUGIN_NAME" VARCHAR(255) NOT NULL,
  "DESCRIPTION" VARCHAR(16777215),
  "PRIORITY" INTEGER,
  "CARDINALITY" VARCHAR(10),
  "PARENT_ID" INTEGER,
  "PLUGIN_CONFIG_KEY" VARCHAR(500),
  "NAME" VARCHAR(200),
  "STATUS" VARCHAR(40),
  "LANGUAGE" VARCHAR(20),
  "NOTE_DATA" CLOB(2147483647),
  "NOTE_USER_LOGIN" VARCHAR(40),
  "NOTE_CREATED_AT" TIMESTAMP,
  "NOTE_UPDATED_AT" TIMESTAMP,
  "CHARACTERISTIC_ID" INTEGER,
  "DEFAULT_CHARACTERISTIC_ID" INTEGER,
  "REMEDIATION_FUNCTION" VARCHAR(20),
  "DEFAULT_REMEDIATION_FUNCTION" VARCHAR(20),
  "REMEDIATION_COEFF" VARCHAR(20),
  "DEFAULT_REMEDIATION_COEFF" VARCHAR(20),
  "REMEDIATION_OFFSET" VARCHAR(20),
  "DEFAULT_REMEDIATION_OFFSET" VARCHAR(20),
  "EFFORT_TO_FIX_DESCRIPTION" VARCHAR(4000),
  "CREATED_AT" TIMESTAMP,
  "UPDATED_AT" TIMESTAMP
  );

  CREATE TABLE "ACTIVE_RULE_CHANGES" (
  "ID" INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "USERNAME" VARCHAR(200),
  "PROFILE_ID" INTEGER NOT NULL,
  "PROFILE_VERSION" INTEGER NOT NULL,
  "RULE_ID" INTEGER NOT NULL,
  "CHANGE_DATE" TIMESTAMP NOT NULL,
  "ENABLED" BOOLEAN,
  "OLD_SEVERITY" INTEGER,
  "NEW_SEVERITY" INTEGER
  );

  CREATE TABLE "ACTIVE_RULES" (
  "ID" INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "PROFILE_ID" INTEGER NOT NULL,
  "RULE_ID" INTEGER NOT NULL,
  "FAILURE_LEVEL" INTEGER NOT NULL,
  "INHERITANCE" VARCHAR(10),
  "NOTE_CREATED_AT" TIMESTAMP,
  "NOTE_UPDATED_AT" TIMESTAMP,
  "NOTE_USER_LOGIN" VARCHAR(40),
  "NOTE_DATA" CLOB(2147483647)
  );

  CREATE TABLE "ACTIVE_RULE_PARAMETERS" (
  "ID" INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "ACTIVE_RULE_ID" INTEGER NOT NULL,
  "RULES_PARAMETER_ID" INTEGER NOT NULL,
  "RULES_PARAMETER_KEY" VARCHAR(128),
  "VALUE" VARCHAR(4000)
  );

  CREATE TABLE "USERS" (
  "ID" INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "LOGIN" VARCHAR(40),
  "NAME" VARCHAR(200),
  "EMAIL" VARCHAR(100),
  "CRYPTED_PASSWORD" VARCHAR(40),
  "SALT" VARCHAR(40),
  "CREATED_AT" TIMESTAMP,
  "UPDATED_AT" TIMESTAMP,
  "REMEMBER_TOKEN" VARCHAR(500),
  "REMEMBER_TOKEN_EXPIRES_AT" TIMESTAMP,
  "ACTIVE" BOOLEAN DEFAULT TRUE
  );

  CREATE TABLE "ACTIVITIES" (
  "LOG_KEY" VARCHAR(250),
  "CREATED_AT" TIMESTAMP,
  "USER_LOGIN" VARCHAR(30),
  "LOG_TYPE" VARCHAR(250),
  "LOG_ACTION" VARCHAR(250),
  "LOG_MESSAGE" VARCHAR(250),
  "DATA_FIELD" CLOB(2147483647)
  );