/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __Utilities_Connector_h__
#define __Utilities_Connector_h__

#include <list>
#include <algorithm>
#include "SafeIteration.h"

namespace mailiverse {
namespace utilities {
namespace connector {

template<class F, class M>
class Male
{
	private:
		F *connection;

	public:
		void connect (F *connection) { connection->addConnection ((M*)this); this->connection=connection; onConnect (); }
		void disconnect () { onDisconnect(); connection->removeConnection((M*)this); this->connection=NULL; }

	protected:
		virtual void onConnect () { };
		virtual void onDisconnect () { };

	public:
		Male () { connection = NULL; }
		virtual ~Male () { if (connection) disconnect(); }

		F *getConnection () const { return connection; }
} ;

template<class F, class M>
class Female
{
	friend class Male<F,M>;

	protected:
		typedef SafeIteration<std::list<M *> > ConnectionList;
		ConnectionList connections;

	private:
		void addConnection (M *connection) 
		{ 
			if (std::find(connections.begin(), connections.end(), connection)==connections.end())
			{
				connections.push_front (connection);
				onConnect (connection);
			}
		}

		void removeConnection (M *connection)
		{
			typename ConnectionList::iterator i =
				std::find (connections.begin(), connections.end(), connection);

			if (i!=connections.end())
			{
				onDisconnect (connection);
				connections.erase (i);
			}
		}

	protected:
		virtual void onConnect (M *connection) { };
		virtual void onDisconnect (M *connection) { };

	public:
		virtual ~Female ()
		{
			clearConnections();
		}

		void clearConnections ()
		{
			while (!connections.empty())
				connections.front()->disconnect();
		}
} ;

} // namespace connector
} // namespace utilities
} // namespace

#endif
