import React, { PropTypes } from 'react';

import { SelectableLabel }  from '../selectable-label';
import EditableTextForm     from './EditableTextForm';

class EditableText extends React.Component {
  render() {
    return this.props.editing
      ? <EditableTextForm
          className={this.props.className}
          loading={this.props.loading}
          text={this.props.text}
          selectTextOnEdit={this.props.selectTextOnEdit}
          onSubmit={this.props.onSubmit}
          onCancel={this.props.onToggleEdit}
        />
      : <p className={this.props.className}>
          <span>
            <span
              className="editable-text__edit-icon btn--icon edit-btn"
              onClick={this.props.onToggleEdit}
            >
              <i className="fa fa-edit" />
            </span>
            <SelectableLabel label={this.props.text} />
          </span>
        </p>;
  }
};

EditableText.propTypes = {
  className: PropTypes.string,
  loading: PropTypes.bool.isRequired,
  editing: PropTypes.bool.isRequired,
  text: PropTypes.string.isRequired,
  selectTextOnEdit: PropTypes.bool.isRequired,
  onSubmit: PropTypes.func.isRequired,
  onToggleEdit: PropTypes.func.isRequired,
};

export default EditableText;
