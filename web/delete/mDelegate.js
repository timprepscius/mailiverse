
var mDelegate = 
{
	signal: function(_e,p) 
	{
		e = '' + _e;
		log('signal ',e,p);
	},

};

for (x in mDelegateCommon)
{
	mDelegate[x] = mDelegateCommon[x];
}