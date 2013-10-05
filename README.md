mailiverse
==========

This documents talks about installing mailiverse from an origin to a target.

------- Requirements on the origin build machine ---------

To install from and build mailiverse from the origin, the origin must:

1. Have ssh keys:
ssh-keygen -t rsa -b 4096

2. Have git installed:
sudo apt-get install git --yes

3. Have unzip installed:
sudo apt-get install unzip --yes

4. Clone the mailiverse:
git clone https://github.com/timprepscius/mailiverse.git

5. Have oracle java installed:
cd mailiverse/install && sudo ./setup-java.remote

6. Have ant installed:
sudo apt-get install ant --yes

7.  Add the line to the hosts file for the target machine:

So let's say your target machine was named: joesmail.com
And it's IP was: 192.168.1.243

sudo nano /etc/hosts
add the lines:
192.168.1.243 mail.joesmail.com

This could also be done on a DNS server, if you control one..



After these steps have been taken you are ready to set up.




--------- Setting up -------------

1. ./setup-1-dependencies

This will build enough stuff to generate keys for things.
It will will generate default keys, default passwords, etc.
You will at some point need to type in "password" a bunch of times, to export a particular
key so it can be used with nginx.

2. ./setup-2-install

This will install the mailiverse system onto the target.
This means it will create user directories, install software, etc.


3. ./setup-3-compile

This will compile all the sources, jar them up, war them up, etc.


4. ./setup-4-deploy

This will deploy mailiverse onto the target machine.


---------

Congratulations, if you have finished these, you should be able to put:

https://mail.targetmachine/

And it should work.
