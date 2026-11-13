import { formatMoney } from '../utils/format.js'

function BudgetEstimator({ maxBudget, nights, onChange }) {
  return (
    <div className="budget-planner">
      <div className="budget-planner-copy">
        <p className="eyebrow">TRIP BUDGET ESTIMATOR</p>
        <p>
          Compare estimated room totals for {nights} {nights === 1 ? 'night' : 'nights'}.
          {maxBudget !== '' && ` Showing stays up to ${formatMoney(Number(maxBudget))}.`}
        </p>
        <small>One room, based on the listed nightly price. Your final total is confirmed before booking.</small>
      </div>
      <label className="form-field budget-field">
        <span>Maximum room budget (optional)</span>
        <input
          type="number"
          min="0"
          step="500"
          placeholder="No limit"
          value={maxBudget}
          onChange={(event) => onChange(event.target.value)}
        />
      </label>
    </div>
  )
}

export default BudgetEstimator
