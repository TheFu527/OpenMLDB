//
// zk_client.h
// Copyright (C) 2017 4paradigm.com
// Author vagrant
// Date 2017-09-04
//


#ifndef RTIDB_ZK_CLIENT_H
#define RTIDB_ZK_CLIENT_H

#include "boost/function.hpp"
#include "mutex.h"

extern "C" {
#include "zookeeper/zookeeper.h"
}

using ::baidu::common::MutexLock;
using ::baidu::common::Mutex;

namespace rtidb {
namespace zk {

typedef boost::function<void (const std::vector<std::string>& endpoint)> NodesChangedCallback;

class ZkClient {

public:

    // hosts , the zookeeper server lists eg host1:2181,host2:2181
    // session_timeout, the session timeout
    // endpoint, the client endpoint
    ZkClient(const std::string& hosts, 
             int32_t session_timeout,
             const std::string& endpoint,
             const std::string& zk_root_path);

    ~ZkClient();

    // init zookeeper connections
    bool Init();

    // the client will create a ephemeral sequence node in zk_root_path
    // eg {zk_root_path}/nodes/000000 -> endpoint
    bool Register();

    // always watch the all nodes in {zk_root_path}/nodes/
    void WatchNodes(NodesChangedCallback callback);

    void HandleNodesChanged();

    // get all alive nodes
    bool GetNodes(std::vector<std::string>& endpoints);

    void LogEvent(int type, int state, const char* path);

private:

    // input args
    std::string hosts_;
    int32_t session_timeout_;
    std::string endpoint_;
    std::string zk_root_path_;

    // internal args
    std::string nodes_root_path_;
    std::vector<NodesChangedCallback> nodes_watch_callbacks_;

    //
    Mutex mu_;
    zhandle_t* zk_;
};

}
}

#endif /* !RTIDB_ZK_CLIENT_H */
