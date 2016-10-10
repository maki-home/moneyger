import React, {Component} from "react";
import {Table, TableCell} from "pui-react-table";
import {Icon} from "pui-react-iconography";
import {DefaultButton, DangerButton} from "pui-react-buttons";
import format from "number-format.js";

const AmountCell = props => (
    <TableCell>
        {format('#,##0.####', props.value)}円
        x {props.rowDatum.quantity} {props.rowDatum.creditCard &&
    <Icon name="credit-card" size="sm"/>}
    </TableCell>
);

const CategoryCell = props => (
    <TableCell>
        {props.value.parentOutcomeCategory.parentCategoryName}
        <br />
        {props.value.categoryName}
    </TableCell>
);

const OutcomeByCell = members => {
    return props=> (<TableCell>{members[props.value]}</TableCell>)
};

const OperationsCell = props =>(
    <TableCell>
        <DefaultButton><Icon name="edit" size="sm"/></DefaultButton>
        <DangerButton><Icon name="trash" size="sm"/></DangerButton>
    </TableCell>
);

class OutcomeTable extends Component {
    constructor(props) {
        super(props);
        this.state = {
            data: props.data,
            members: []
        };
    }

    render() {
        let columns = [
            {
                attribute: 'outcomeDate',
                displayName: '支出日',
                sortable: true
            },
            {
                attribute: 'outcomeCategory',
                displayName: 'カテゴリ',
                sortable: true,
                sortBy: value => value.categoryId,
                CustomCell: CategoryCell
            },
            {
                attribute: 'outcomeName',
                displayName: '支出名',
                sortable: true
            },
            {
                attribute: 'amount',
                displayName: '金額',
                sortable: true,
                sortBy: value => Number(value),
                CustomCell: AmountCell
            },
            {
                attribute: 'outcomeBy',
                displayName: '支出者',
                sortable: true,
                CustomCell: OutcomeByCell(this.state.members)
            },
            {
                attribute: 'operations',
                displayName: '操作',
                sortable: false,
                CustomCell: OperationsCell
            }
        ];
        return (<Table columns={columns} data={this.state.data}/>);
    }

    componentDidMount() {
        this.props.fetchMembers
            .then(members => {
                this.setState({
                    members: members.reduce((o, m)=> {
                        o[m.memberId] = `${m.familyName} ${m.givenName}`;
                        return o;
                    }, {})
                });
            });
    }

    componentWillReceiveProps(props) {
        this.setState({
            data: props.data
        });
    }
}

export default OutcomeTable;