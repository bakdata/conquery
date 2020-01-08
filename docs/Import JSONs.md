
# Import JSONs
This is an automatically created documentation. It is not 100% accurate since the generator does not handle every edge case.

Instead of a list ConQuery also always accepts a single element.

Each `*.import.json` has to contain exactly one [ImportDescriptor](#Type-ImportDescriptor).


---

## Base Output


Different types of Output can be used by setting `operation` to one of the following values:


### CONCAT<sup><sub><sup> [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/ConcatOutput.java)</sup></sub></sup>


<details><summary>Details</summary><p>

Java Type: `com.bakdata.conquery.models.preproc.outputs.ConcatOutput`

Supported Fields:

|  | Field | Type | Default | Example | Description |
| --- | --- | --- | --- | --- | --- |
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/ConcatOutput.java) | inputColumns | list of `int` | `null` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/Output.java) | name | `String` | `null` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/Output.java) | required | `boolean` | `false` |  |  | 
</p></details>

### COPY<sup><sub><sup> [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/CopyOutput.java)</sup></sub></sup>


<details><summary>Details</summary><p>

Java Type: `com.bakdata.conquery.models.preproc.outputs.CopyOutput`

Supported Fields:

|  | Field | Type | Default | Example | Description |
| --- | --- | --- | --- | --- | --- |
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/CopyOutput.java) | inputColumn | `int` | `0` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/CopyOutput.java) | inputType | one of STRING, INTEGER, BOOLEAN, REAL, DECIMAL, MONEY, DATE, DATE_RANGE | `null` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/Output.java) | name | `String` | `null` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/Output.java) | required | `boolean` | `false` |  |  | 
</p></details>

### DATE_RANGE<sup><sub><sup> [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/DateRangeOutput.java)</sup></sub></sup>


<details><summary>Details</summary><p>

Java Type: `com.bakdata.conquery.models.preproc.outputs.DateRangeOutput`

Supported Fields:

|  | Field | Type | Default | Example | Description |
| --- | --- | --- | --- | --- | --- |
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/DateRangeOutput.java) | endColumn | `int` | `-1` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/DateRangeOutput.java) | startColumn | `int` | `-1` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/Output.java) | name | `String` | `null` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/Output.java) | required | `boolean` | `false` |  |  | 
</p></details>

### EPOCH<sup><sub><sup> [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/EpochOutput.java)</sup></sub></sup>


<details><summary>Details</summary><p>

Java Type: `com.bakdata.conquery.models.preproc.outputs.EpochOutput`

Supported Fields:

|  | Field | Type | Default | Example | Description |
| --- | --- | --- | --- | --- | --- |
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/EpochOutput.java) | column | `int` | `0` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/Output.java) | name | `String` | `null` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/Output.java) | required | `boolean` | `false` |  |  | 
</p></details>

### EPOCH_DATE_RANGE<sup><sub><sup> [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/EpochDateRangeOutput.java)</sup></sub></sup>


<details><summary>Details</summary><p>

Java Type: `com.bakdata.conquery.models.preproc.outputs.EpochDateRangeOutput`

Supported Fields:

|  | Field | Type | Default | Example | Description |
| --- | --- | --- | --- | --- | --- |
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/EpochDateRangeOutput.java) | endColumn | `int` | `-1` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/EpochDateRangeOutput.java) | startColumn | `int` | `-1` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/Output.java) | name | `String` | `null` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/Output.java) | required | `boolean` | `false` |  |  | 
</p></details>

### LINE<sup><sub><sup> [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/LineOutput.java)</sup></sub></sup>


<details><summary>Details</summary><p>

Java Type: `com.bakdata.conquery.models.preproc.outputs.LineOutput`

Supported Fields:

|  | Field | Type | Default | Example | Description |
| --- | --- | --- | --- | --- | --- |
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/Output.java) | name | `String` | `null` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/Output.java) | required | `boolean` | `false` |  |  | 
</p></details>

### NULL<sup><sub><sup> [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/NullOutput.java)</sup></sub></sup>


<details><summary>Details</summary><p>

Java Type: `com.bakdata.conquery.models.preproc.outputs.NullOutput`

Supported Fields:

|  | Field | Type | Default | Example | Description |
| --- | --- | --- | --- | --- | --- |
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/NullOutput.java) | inputType | one of STRING, INTEGER, BOOLEAN, REAL, DECIMAL, MONEY, DATE, DATE_RANGE | `null` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/Output.java) | name | `String` | `null` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/Output.java) | required | `boolean` | `false` |  |  | 
</p></details>

### QUARTER_TO_FIRST_DAY<sup><sub><sup> [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/QuarterToFirstDayOutput.java)</sup></sub></sup>


<details><summary>Details</summary><p>

Java Type: `com.bakdata.conquery.models.preproc.outputs.QuarterToFirstDayOutput`

Supported Fields:

|  | Field | Type | Default | Example | Description |
| --- | --- | --- | --- | --- | --- |
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/Output.java) | name | `String` | `null` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/Output.java) | required | `boolean` | `false` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/QuarterToFirstDayOutput.java) | quarterColumn | `int` | `0` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/QuarterToFirstDayOutput.java) | yearColumn | `int` | `0` |  |  | 
</p></details>

### QUARTER_TO_RANGE<sup><sub><sup> [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/QuarterToRangeOutput.java)</sup></sub></sup>


<details><summary>Details</summary><p>

Java Type: `com.bakdata.conquery.models.preproc.outputs.QuarterToRangeOutput`

Supported Fields:

|  | Field | Type | Default | Example | Description |
| --- | --- | --- | --- | --- | --- |
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/Output.java) | name | `String` | `null` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/Output.java) | required | `boolean` | `false` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/QuarterToRangeOutput.java) | quarterColumn | `int` | `0` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/QuarterToRangeOutput.java) | yearColumn | `int` | `0` |  |  | 
</p></details>

### SOURCE<sup><sub><sup> [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/SourceOutput.java)</sup></sub></sup>


<details><summary>Details</summary><p>

Java Type: `com.bakdata.conquery.models.preproc.outputs.SourceOutput`

Supported Fields:

|  | Field | Type | Default | Example | Description |
| --- | --- | --- | --- | --- | --- |
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/Output.java) | name | `String` | `null` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/Output.java) | required | `boolean` | `false` |  |  | 
</p></details>

### STARTING_FROM<sup><sub><sup> [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/StartingFromOutput.java)</sup></sub></sup>


<details><summary>Details</summary><p>

Java Type: `com.bakdata.conquery.models.preproc.outputs.StartingFromOutput`

Supported Fields:

|  | Field | Type | Default | Example | Description |
| --- | --- | --- | --- | --- | --- |
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/Output.java) | name | `String` | `null` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/Output.java) | required | `boolean` | `false` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/StartingFromOutput.java) | inputColumn | `int` | `-1` |  |  | 
</p></details>

### UNPIVOT<sup><sub><sup> [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/UnpivotOutput.java)</sup></sub></sup>


<details><summary>Details</summary><p>

Java Type: `com.bakdata.conquery.models.preproc.outputs.UnpivotOutput`

Supported Fields:

|  | Field | Type | Default | Example | Description |
| --- | --- | --- | --- | --- | --- |
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/Output.java) | name | `String` | `null` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/Output.java) | required | `boolean` | `false` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/UnpivotOutput.java) | includeNulls | `boolean` | `false` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/UnpivotOutput.java) | inputColumns | list of `int` | `null` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/outputs/UnpivotOutput.java) | inputType | one of STRING, INTEGER, BOOLEAN, REAL, DECIMAL, MONEY, DATE, DATE_RANGE | `null` |  |  | 
</p></details>



---

## Other Types

### Type ImportDescriptor<sup><sub><sup> [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/ImportDescriptor.java)</sup></sub></sup>


<details><summary>Details</summary><p>

Java Type: `com.bakdata.conquery.models.preproc.ImportDescriptor`

Supported Fields:

|  | Field | Type | Default | Example | Description |
| --- | --- | --- | --- | --- | --- |
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/identifiable/Labeled.java) | label | `String` | `null` | `"someLabel"` | shown in the frontend | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/identifiable/NamedImpl.java) | name | `String` | `null` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/ImportDescriptor.java) | inputs | list of [Input](#Type-Input) | `null` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/ImportDescriptor.java) | table | `String` | `null` |  |  | 
</p></details>

### Type Input<sup><sub><sup> [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/Input.java)</sup></sub></sup>


<details><summary>Details</summary><p>

Java Type: `com.bakdata.conquery.models.preproc.Input`

Supported Fields:

|  | Field | Type | Default | Example | Description |
| --- | --- | --- | --- | --- | --- |
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/Input.java) | autoOutput | `AutoOutput` | `null` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/Input.java) | filter | `String` | `null` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/Input.java) | output | list of [Output](#Base-Output) | `null` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/Input.java) | primary | [Output](#Base-Output) | `null` |  |  | 
| [✎](https://github.com/bakdata/conquery/edit/develop/backend/src/main/java/com/bakdata/conquery/models/preproc/Input.java) | sourceFile | `File` | `null` |  |  | 
</p></details>
