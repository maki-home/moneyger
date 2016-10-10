import axios from "axios";

class OutcomeClient {
    findByOutcomeDate(outcomeDate) {
        return axios.get('/api/outcomes/search/findByOutcomeDate', {
            params: {
                fromDate: outcomeDate.format('YYYY-MM-DD'),
                toDate: outcomeDate.format('YYYY-MM-DD')
            }
        }).then(x => {
            return x.data._embedded.outcomes;
        });
    }

    fetchOutcomeCategories() {
        return axios.get('/api/outcomeCategories')
            .then(x => {
                return x.data._embedded.outcomeCategories;
            });
    }

    addOutcome(outcome) {
        return axios.post('/api/outcomes', outcome)
            .then(x => {
                const link = x.data._links.outcomeCategory.href,
                    nextUrl = link.substring(link.lastIndexOf('/api'));
                return axios.get(nextUrl)
                    .then(y => {
                        let outcome = x.data;
                        outcome.outcomeCategory = y.data;
                        return outcome;
                    });
            });
    }
}

export default new OutcomeClient();


