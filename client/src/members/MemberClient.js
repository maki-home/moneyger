import axios from "axios";

class MemberClient {
    fetchMembers() {
        return axios.get('/api/members')
            .then(x => {
                return x.data._embedded.members;
            });
    }

    fetchUserinfo() {
        return axios.get('/userinfo')
            .then(x => {
                return x.data;
            });
    }
}

export default new MemberClient();
