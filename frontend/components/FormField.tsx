interface Props {
  label: string;
  name: string;
  type?: string;
  value: string;
  onChange: (value: string) => void;
  required?: boolean;
  error?: string;
  placeholder?: string;
  autoComplete?: string;
}

export function FormField({
  label, name, type = 'text', value, onChange,
  required, error, placeholder, autoComplete,
}: Props) {
  return (
    <div>
      <label htmlFor={name} className="block text-sm font-medium text-slate-300 mb-1">
        {label} {required && <span className="text-rose-400">*</span>}
      </label>
      <input
        id={name}
        name={name}
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        required={required}
        placeholder={placeholder}
        autoComplete={autoComplete}
        className={`w-full px-3 py-2 rounded bg-slate-800 border ${
          error ? 'border-rose-500' : 'border-slate-700'
        } text-slate-100 placeholder-slate-500 focus:outline-none focus:border-blue-500`}
      />
      {error && <p className="text-rose-400 text-xs mt-1">{error}</p>}
    </div>
  );
}