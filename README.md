
# Github sync

What repositories are contributors interested in ?
Given an org find all users and then collect the repositories that they are interested in (starred)

The authenticated user can then star any of these themselves

## Running the app
without env vars (default URL, no auth, disabled logging)

i.e.
```
sbt ~reStart
```

with env vars (GH_TOKEN, GH_URL, LOG_APP, LOG_CLI)

i.e.
```
 GH_TOKEN=${token} sbt ~reStart
```

This will start the app on localhost with port 8080

### Interact with the app
The app only has a single route currently enabled GET /org/{orgName}/contributors

i.e.
```
curl -v http://localhost:8080/org/typelevel/contributors
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

### Tests
Unit Tests
```
sbt test
```

## Notes

* Private repositories are not removed from the list of repos for an org as the authenticated user may have permissions on them. However, if they dont have permissions a 404 is returned thus 404 errors are ignored   

## Design

* Http4s giter [template](https://github.com/http4s/http4s.g8)
* [Tagless Final pattern](https://scalac.io/tagless-final-pattern-for-scala-code/) for core domain
* [Onion architecture](https://jeffreypalermo.com/2008/07/the-onion-architecture-part-1/)
* Inspiration from the scala [petstore](https://github.com/pauljamescleary/scala-pet-store)
* Higher kinded types 
* Error handling with classy optics inspired by [Gabriel Volpe](https://typelevel.org/blog/2018/08/25/http4s-error-handling-mtl.html)

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Thankyou for your consideration 

