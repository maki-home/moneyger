import React, {Component} from "react";
import {Input} from "pui-react-inputs";
import {Icon} from "pui-react-iconography";
import {HighlightButton} from "pui-react-buttons";

class OutcomeForm extends Component {
    constructor(props) {
        super(props);
        this.outcomeDate = props.outcomeDate;
        this.state = {
            outcomeDate: this.outcomeDate.format('YYYY-MM-DD'),
            outcomeName: '',
            amount: '',
            quantity: '1',
            categoryId: '',
            outcomeBy: '',
            creditCard: true,
            isLoading: false,
            validationState: {},
            errorMessage: {},
            calc: {
                amount: 0,
                updated: 0,
                freezed: true
            },
            leaveValue: false,
            members: [],
            outcomeCategories: []
        };
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.addOutcome = props.addOutcome;
    }

    render() {
        return (<form className="form" role="form" onSubmit={this.handleSubmit}>
                <Input label="" id="outcomeDate" placeholder="支出日" type="date"
                       value={this.state.outcomeDate}
                       disabled={this.state.isLoading}
                       displayError={this.state.validationState.outcomeDate === 'error'}
                       errorMessage={this.state.errorMessage.outcomeDate}
                       onChange={this.handleChange}/>
                <Input label="" id="outcomeName" placeholder="支出名"
                       value={this.state.outcomeName}
                       disabled={this.state.isLoading}
                       displayError={this.state.validationState.outcomeName === 'error'}
                       errorMessage={this.state.errorMessage.outcomeName}
                       onChange={this.handleChange}
                       autoFocus={true}/>
                <Input label="" id="amount" placeholder="単価" type="number"
                       value={this.state.amount}
                       disabled={this.state.isLoading}
                       displayError={this.state.validationState.amount === 'error'}
                       errorMessage={this.state.errorMessage.amount}
                       onChange={this.handleChangeAmount.bind(this)}/>
                <Input label="" id="quantity" placeholder="数量" type="number"
                       value={this.state.quantity}
                       disabled={this.state.isLoading}
                       min="0"
                       displayError={this.state.validationState.quantity === 'error'}
                       errorMessage={this.state.errorMessage.quantity}
                       onChange={this.handleChange}/>
                <div
                    className={this.state.validationState.categoryId === 'error' ? "form-group has-error" : "form-group"}>
                    <select className="form-control" id="categoryId"
                            value={this.state.categoryId}
                            disabled={this.state.isLoading}
                            onChange={this.handleChange}>
                        <option value="">カテゴリ</option>
                        {this.state.outcomeCategories.map(c => (
                            <option label={`${c.parentOutcomeCategory.parentCategoryName} (${c.categoryName})`}
                                    value={c.categoryId} key={c.categoryId}/>))}
                    </select>
                    {this.state.validationState.categoryId === 'error' &&
                    <div className="error-text help-block">{this.state.errorMessage.categoryId}</div>}
                </div>
                <div
                    className={this.state.validationState.outcomeBy === 'error' ? "form-group has-error" : "form-group"}>
                    <select className="form-control" id="outcomeBy"
                            value={this.state.outcomeBy}
                            disabled={this.state.isLoading}
                            onChange={this.handleChange}>
                        <option value="">支出者</option>
                        {this.state.members.map(m => (
                            <option label={`${m.familyName} ${m.givenName}`} value={m.memberId} key={m.memberId}/>))}
                    </select>
                    {this.state.validationState.outcomeBy === 'error' &&
                    <div className="error-text help-block">{this.state.errorMessage.outcomeBy}</div>}
                </div>
                <div className="btn-group" role="group">
                    <button type="button" className="btn btn-default" disabled={this.state.isLoading}
                            onClick={this.handleResetCalc.bind(this)}>Reset
                    </button>
                    <button type="button" className="btn btn-default" disabled={this.state.isLoading}
                            onClick={this.handleChangeCalc(8).bind(this)}>
                        +8%
                    </button>
                    <button type="button" className="btn btn-default" disabled={this.state.isLoading}
                            onClick={this.handleChangeCalc(-8).bind(this)}>
                        -8%
                    </button>
                    <button type="button" className="btn btn-default" disabled={this.state.isLoading}
                            onClick={this.handleChangeCalc(5).bind(this)}>
                        +5%
                    </button>
                    <button type="button" className="btn btn-default" disabled={this.state.isLoading}
                            onClick={this.handleChangeCalc(-5).bind(this)}>
                        -5%
                    </button>
                    <button type="button" className="btn btn-default" disabled={this.state.isLoading}
                            onClick={this.handleChangeCalc(10).bind(this)}>
                        +10%
                    </button>
                    <button type="button" className="btn btn-default" disabled={this.state.isLoading}
                            onClick={this.handleChangeCalc(-10).bind(this)}>
                        -10%
                    </button>
                </div>
                <div className="checkbox">
                    <label>
                        <input id="creditCard" type="checkbox"
                               checked={this.state.creditCard}
                               disabled={this.state.isLoading}
                               onChange={this.handleToggle('creditCard').bind(this)}/>
                        <Icon name="credit-card" size="h2"/>
                    </label>
                </div>
                <div className="checkbox">
                    <label>
                        <input id="leaveValue" type="checkbox"
                               checked={this.state.leaveValue}
                               disabled={this.state.isLoading}
                               onChange={this.handleToggle('leaveValue').bind(this)}/> 入力値を残す
                    </label>
                </div>
                <HighlightButton disabled={this.state.isLoading} onClick={this.handleSubmit}>登録</HighlightButton>
            </form>
        )
    }

    handleChange(event) {
        let propertyName = event.target.id;
        let newState = {};
        newState[propertyName] = event.target.value;
        this.setState(newState);
    }

    handleToggle(propertyName) {
        return (event) => {
            const val = this.state[propertyName];
            this.handleChange({
                target: {
                    id: propertyName,
                    value: !val
                }
            });
        }
    }

    handleChangeAmount(event) {
        this.handleChange(event);
        this.setState({
            calc: {
                amount: event.target.value,
                updated: event.target.value,
                freezed: false
            }
        });
    }

    handleChangeCalc(percent) {
        return (event) => {
            if (!this.state.calc.freezed) {
                let calc = this.state.calc;
                calc.updated = Math.floor((percent + 100) * Number(calc.updated) / 100);
                this.setState({calc: calc, amount: calc.updated});
            }
        };
    }

    handleResetCalc(event) {
        if (!this.state.calc.freezed) {
            let calc = this.state.calc;
            calc.updated = calc.amount;
            this.setState({calc: calc, amount: calc.updated});
        }
    }

    isEmpty(propertyName, label, validationState, errorMessage) {
        let value = String(this.state[propertyName]);
        if (value == null || value === "") {
            validationState[propertyName] = 'error';
            errorMessage[propertyName] = `${label}は必須です。`;
            return true;
        } else {
            validationState[propertyName] = '';
            errorMessage[propertyName] = '';
            return false;
        }
    }

    handleSubmit(event) {
        event.preventDefault();
        let submittable = true;
        let validationState = {}, errorMessage = {};
        if (this.isEmpty('outcomeDate', '支出日', validationState, errorMessage)) {
            submittable = false;
        }
        if (this.isEmpty('outcomeName', '支出名', validationState, errorMessage)) {
            submittable = false;
        }
        if (this.isEmpty('amount', '単価', validationState, errorMessage)) {
            submittable = false;
        }
        if (this.isEmpty('quantity', '数量', validationState, errorMessage)) {
            submittable = false;
        }
        if (this.isEmpty('categoryId', 'カテゴリ', validationState, errorMessage)) {
            submittable = false;
        }
        if (this.isEmpty('outcomeBy', '支出者', validationState, errorMessage)) {
            submittable = false;
        }
        this.setState({
            validationState: validationState,
            errorMessage: errorMessage
        });
        if (submittable) {
            const requestBody = {
                outcomeDate: this.state.outcomeDate,
                outcomeName: this.state.outcomeName,
                amount: this.state.amount,
                quantity: this.state.quantity,
                outcomeCategory: {
                    categoryId: this.state.categoryId
                },
                outcomeBy: this.state.outcomeBy,
                creditCard: this.state.creditCard
            };
            this.setState({
                isLoading: true
            });
            this.addOutcome(requestBody)
                .then(outcome => {
                    let state = {
                        outcomeDate: this.state.outcomeDate,
                        outcomeName: this.state.outcomeName,
                        amount: this.state.amount,
                        quantity: this.state.quantity,
                        categoryId: this.state.categoryId,
                        outcomeBy: this.state.outcomeBy,
                        creditCard: this.state.creditCard
                    };
                    if (!this.state.leaveValue) {
                        state.outcomeName = '';
                        state.amount = '';
                        state.quantity = '1';
                        state.categoryId = '';
                    }
                    state.isLoading = false;
                    this.setState(state);
                });
        }
    }

    componentDidMount() {
        // TODO Promise.all
        this.props.fetchUserinfo
            .then(userinfo => {
                this.setState({
                    outcomeBy: userinfo.id
                });
            });
        this.props.fetchMembers
            .then(members => {
                this.setState({
                    members: members
                });
            });
        this.props.fetchOutcomeCategories
            .then(outcomeCategories => {
                this.setState({
                    outcomeCategories: outcomeCategories
                });
            });
    }

    componentWillReceiveProps(props) {
        const outcomeDate = props.outcomeDate;
        this.setState({
            outcomeDate: outcomeDate.format('YYYY-MM-DD')
        });
    }
}

export default OutcomeForm;