import java.text.SimpleDateFormat

class FormatTagLib {
	def dateFormat = { attrs, body ->
		out << new SimpleDateFormat(attrs.format).format(attrs.date)
	}
}