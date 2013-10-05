mailiverse
==========

This documents talks about installing mailiverse from an origin to a target.

------- Requirements on the origin build machine ---------

To install from and build mailiverse from the origin, the origin must
have "git", "ant", "rsync" and "oracle java" installed.  (Other javas might work, but I have
not tested them.)  Also, your hosts file must point to mail.target and target.

I have always used Ubuntu 12.04 or 13 as my target machine.
The build works on Ubuntu 12.04 and also OSX (with dev tools).


To install git:
sudo apt-get install git --yes


To install zip:
sudo apt-get install unzip --yes


To install ant:
sudo apt-get install ant --yes

Clone the mailiverse:
git clone https://github.com/timprepscius/mailiverse.git


To install java:
cd mailiverse/install
sudo ./setup-java.remote


To make the hosts file point to the correct place:
sudo nano /etc/hosts
add the lines:
SOME-IP-ADDRESS mail.whateverthenameofyourdomainis
SOME-IP-ADDRESS whateverthenameofyourdomainis

This could also be done on a DNS server, if you control one..



After these steps have been taken you are ready to set up.




--------- Setting up -------------

First run:
./setup-1-dependencies

This will build enough stuff to generate keys for things.
It will will generate default keys, default passwords, etc.
You will at some point need to type in "password" a bunch of times, to export a particular
key so it can be used with nginx.

Then run:
./setup-2-install

This will install the mailiverse system onto the target.
This means it will create user directories, install software, etc.


Then run:
./setup-3-compile

This will compile all the sources, jar them up, war them up, etc.


Then run:
./setup-4-deploy

This will deploy mailiverse onto the target machine.


---------

Congratulations, if you have finished these, you should be able to put:

https://mail.targetmachine/

And it should work.
