# Test Travis Continuous Integration

[![Build Status](https://travis-ci.org/xlui/test-travis-ci.svg?branch=master)](https://travis-ci.org/xlui/test-travis-ci)

This repository shows how to use [Travis Continuous Integration](https://travis-ci.org/) in your project.

## Write in Front

In my last project, I used to test and deploy in such processes: write code in a local environment, test at local, use maven to package the project into a `war` file, upload it to the deploy server, deploy it, and restart tomcat. Doing this process cost at least 10 minutes. And in addition, if I find an error after a deploy, I need to deploy again...

This makes me tired, and now I'd like to try some continuous integration solutions such as travis and jenkins. I'm preffer to travis because it is closer to github.

## Register Travis

Go to the official website of travis and register with your github account.

![Travis Register](https://xlui.me/images/travis-register.png "Travis Registeration Page")

After your registration, go to you profile page(this is automate) and choose one of you open source projects and click to open travis ci service.

## Create a java web project

I'll create a simple java web project based on spring boot and spring mvc.

### Directory Tree

```
│  .gitignore
│  .travis.yml
│  LICENSE
│  README.md
│
└─Hello
    │  .gitignore
    │  HelloTravisCI.iml
    │  mvnw
    │  mvnw.cmd
    │  pom.xml
    │
    ├─.idea
    │  └─....
    │
    ├─.mvn
    │  └─wrapper
    │          maven-wrapper.jar
    │          maven-wrapper.properties
    │
    ├─src
    │  ├─main
    │  │  ├─java
    │  │  │  └─me
    │  │  │      └─xlui
    │  │  │          └─spring
    │  │  │                  Application.java
    │  │  │                  HelloController.java
    │  │  │
    │  │  └─resources
    │  │      │  application.properties
    │  │      │
    │  │      ├─static
    │  │      └─templates
    │  └─test
    │      └─java
    │          └─me
    │              └─xlui
    │                  └─spring
    │                          ApplicationTests.java
    │
    └─target
        └─....
```

### HelloController

```java
package me.xlui.spring;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return "<html>" +
                "<head><title>Test Page</title></head>" +
                "<body><div align=\"center\">Hello World!</div><br><br><div align=\"center\">This website shows you have successfully integrated <b>Travis-CI</b></div>" +
                "</body></html>";
    }
}
```

### Tests

```java
package me.xlui.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

    @Test
    public void contextLoads() {
        System.out.println("This is a simple test, and you pass it.");
    }

}
```

## Add .travis.yml

Travis depends on `.travis.yml` in your project to do some actions.

```yml
language: java
jdk:
 - openjdk8
install: cd Hello && mvn install -DskipTests=true -Dmaven.javadoc.skip=true
script: mvn test
```

Note that we create the java web project under the folder `Hello`, so as the value of `install`(always is a bash command), we need to change our directory to `Hello` first and then run `mvn install`.

## Trigger Build

Commit and push the code to github, travis will automatically build this maven project, and you can see the output of build in travis:

![Travis Build](https://xlui.me/images/travis-build.png "Travis Build")

## Automatically Deploy

Now travis can do build pocess automatically after our commits to github, and next we want travis to do auto-deploy.

Travis provide us an option `after_success` to do that.

But pause here. We want travis to auto-deploy projects to our server, this means that we need travis to access our server automatically. This project is an open source project, so expose our secret key to github is stupid. How can we solve it?

### Encrypt your password

Travis provide a solution in the [docs](https://docs.travis-ci.com/user/encrypting-files/), let's try it:

**You should do all things below in you local environment !!!**

First, use `gem` of `ruby` to install `travis`:

```bash
# install ruby
sudo yum install ruby ruby-devel
# update gem
sudo gem update --system
# add ruby-china source, skip this step if you are not chinese users
sudo gem sources --add https://gems.ruby-china.org/
# install travis
sudo gem install travis
```

And then login to travis through command line:

```
$ travis login
We need your GitHub login to identify you.
This information will not be sent to Travis CI, only to api.github.com.
The password will not be displayed.

Try running with --github-token or --auto if you don't want to enter your password anyway.

Username: xlui
Password for xlui: *********************
Two-factor authentication code for xlui: ******
Successfully logged in as xlui!
```

Input your github username, password, two-factor code(only if you have enable it before).

Change the directory to your project directory.

In order to enable travis to access your server, we should encrypt our **local** ssh secret key(So I think we should use public key to login to server, and this should be the only way).

```
$ cd test-travis-ci/
$ travis encrypt-file ~/.ssh/id_rsa --add
Detected repository as xlui/test-travis-ci, is this correct? |yes| yes
encrypting ~/.ssh/id_rsa for xlui/test-travis-ci
storing result as id_rsa.enc
storing secure env variables for decryption

Make sure to add id_rsa.enc to the git repository.
Make sure not to add ~/.ssh/id_rsa to the git repository.
Commit all changes to your .travis.yml.
```

And now check the changes in `.travis.yml`:

```yml
before_install:
  - openssl aes-256-cbc -K $encrypted_9655a05d2431_key -iv $encrypted_9655a05d2431_iv
  -in id_rsa.enc -out ~\/.ssh/id_rsa -d
```

In order to ensure ssh login would not be failed due to permissions, we need to set the permission of secret key manually.

```yml
before_install:
  - openssl aes-256-cbc -K $encrypted_9655a05d2431_key -iv $encrypted_9655a05d2431_iv
  -in id_rsa.enc -out ~/.ssh/id_rsa -d
  - chmod 600 ~/.ssh/id_rsa
```

Also, because of the first login to remote server will have to verify host, we cannot control travis to do that. We need to add `addons` to skip it:

```yml
addons:
  ssh_known_hosts: your_ip_addr:port
```

And note that if your server's ssh port is `22`(which is very not recommend), you can set the value to `your_ip_addr` only.

All things done, now travis can access your server without password authentication!

### Deploy Scripts

Thus travis can access your server without any limits, we can write a deploy script and run it through travis.

```bash
echo "Auto Deploy Success" >> a.log
```

Don't forget to add **run** permission to the script.

### Run Deploy Script

Add the following lines to `.travis.yml`:

```yml
after_success:
  - ssh your-username@your_ip_addr -p your_port "./your_shell-script.sh"
```

replace `your-username`, `your_id_addr`, `your_port`, `your_shell_script` to your own.

## Automatically deploy project to Tomcat

So if you want to do automatically deploy your project with a tomcat contianer, you just need to change the deploy script.

## LICENSE

See [LICENSE](LICENSE)
