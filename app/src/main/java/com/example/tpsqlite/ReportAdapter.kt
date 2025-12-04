package com.example.tpsqlite

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

class ReportAdapter(
    private val context: Context,
    private val reports: ArrayList<Report>
) : BaseAdapter() {

    override fun getCount(): Int = reports.size
    override fun getItem(position: Int): Any = reports[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.report_item, parent, false)

        val tvInfo = view.findViewById<TextView>(R.id.tvReportInfo)
        val btnUpdate = view.findViewById<Button>(R.id.btnUpdateReport)
        val report = reports[position]

        tvInfo.text = "${report.titre} - ${report.categorie} - ${report.priorite}"
        tvInfo.text = "${report.titre} - ${report.categorie} - ${report.priorite}"

        btnUpdate.setOnClickListener {
            val intent = Intent(context, AddReportActivity::class.java)
            intent.putExtra("titre", report.titre)
            intent.putExtra("description", report.description)
            intent.putExtra("categorie", report.categorie)
            intent.putExtra("priorite", report.priorite)
            context.startActivity(intent)
        }

        return view
    }
}
