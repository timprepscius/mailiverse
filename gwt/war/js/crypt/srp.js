function SRPClient(state)
{
	this.state = state;
}

SRPClient.prototype.deserialize = function ()
{
	if (!this.imp)
		this.imp = core.srp.SRPClientSession.fromJSON(JSON.stringify(this.state));
}

SRPClient.prototype.serialize = function ()
{
	return this.state;
}

SRPClient.prototype.transferState = function (jsonString)
{
	var json = JSON.parse(jsonString);
	for (i in json)
		this.state[i] = json[i];
}

SRPClient.prototype.setPassword = function (password64)
{
	this.state.p = password64;
	return this;
}

SRPClient.prototype.setSalt = function (salt)
{
	this.deserialize();
	this.imp.setSalt(salt);
	this.transferState(this.imp.toJSON());
	return this;
}

SRPClient.prototype.setServerPublicKey = function (salt)
{
	this.deserialize();
	this.imp.setServerPublicKey(salt);
	this.transferState(this.imp.toJSON());
	return this;
}

SRPClient.prototype.validateServerEvidence = function (evidence)
{
	this.deserialize();
	this.imp.validateServerEvidence(evidence);
	this.transferState(this.imp.toJSON());
	return this;
}

SRPClient.prototype.getSessionKey = function ()
{
	return this.state.K;
}

SRPClient.prototype.getPublicKey = function ()
{
	return this.state.A;
}

SRPClient.prototype.getEvidenceValue = function ()
{
	return this.state.M1;
}

