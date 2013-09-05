
/<LINK_CONSTANTS>/ {
  r ../web/common/Link.ConstantsDev.html
  d
}

/{##TRUSTSTORE_AUTH##}/ {
  r quote
  r ../config/truststores/auth.jks.b64
  r quote
  r comma
  d
}

/{##TRUSTSTORE_SEND##}/ {
  r quote
  r ../config/truststores/send.jks.b64
  r quote
  r comma
  d
}
