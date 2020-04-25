

CREATE TABLE repositories (
  name VARCHAR ,
  owner VARCHAR,
  PRIMARY KEY(name, owner)
);

CREATE TABLE stargazers (
  name VARCHAR ,
  repo VARCHAR,
  PRIMARY KEY(name, repo)
);

CREATE TABLE registered(
  name VARCHAR PRIMARY KEY
);
