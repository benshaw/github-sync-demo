
# Github sync

Monitor stargazers of an org in GitHub

* Find all repos for an org and then all stargazers of those repos
* syncs all repos and stargazers locally if not already
* uses notificiations (via webhooks)
* utilize streams where possible


## Requirements to run locally
* Postgres used for local storage 

 ```dtd
 docker run --rm --name my_postgres -e POSTGRES_PASSWORD=password -v my_dbdata:/var/lib/postgresql/data -p 5432:5432 postgres:11 -c log_statement=all ```
```
 
* For GitHub web hooks to work the machine your running from need to be public, I use [ngrok](https://ngrok.com/) for this

 ```dtd
ngrok http 8080
```

## Running the app
without env vars (default URL, no auth, disabled logging)

i.e.
```
sbt ~reStart
```

with env vars (GH_TOKEN, GH_URL, LOG_APP, LOG_CLI, HOST)

i.e.
```
 GH_TOKEN=${token} sbt ~reStart
```

This will start the app on localhost with port 8080

### Interact with the app
GET /org/{orgName}/starred

i.e.
```
 curl -v "localhost:8080/org/shawsolutions/starred"
```

### Env Vars
* GH_TOKEN
    * The authentication token used when sending requests to GitHub
* GH_URL
    * The GitHub URL
* LOG_APP
     * Enable Application(requests to and from the application) logging (disabled by default)    
* LOG_CLI
     * Enable Client (from the app to github) logging (disabled by default)   
* HOST
    * Callback URL for webhooks 

### Tests
Unit Tests
```
sbt test
```

## Design

* [Http4s](https://github.com/http4s/http4s) giter [template](https://github.com/http4s/http4s.g8)
* Cats and Cats-Effect
* Doobie for Db and FS2 for streams
* [Tagless Final pattern](https://scalac.io/tagless-final-pattern-for-scala-code/) for core domain
* Inspiration from the scala [petstore](https://github.com/pauljamescleary/scala-pet-store)
* Higher kinded types 
* Error handling with classy optics inspired by [Gabriel Volpe](https://typelevel.org/blog/2018/08/25/http4s-error-handling-mtl.html)


## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

