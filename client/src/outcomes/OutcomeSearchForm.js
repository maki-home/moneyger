import React, {Component} from "react";
import {Input} from "pui-react-inputs";

class OutcomeSearchForm extends Component {
    render() {
        return (<form className="form-inline" role="form">
            <Input
                search
                label=""
            />
        </form>);
    }
}

export default OutcomeSearchForm;