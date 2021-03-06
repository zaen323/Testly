package com.zaen.testly.cas.children.createSet.fragments

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.*
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.LinearLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.firestore.*
import com.zaen.testly.R
import com.zaen.testly.TestlyFirestore
import com.zaen.testly.TestlyUser
import com.zaen.testly.base.fragments.BaseFragment
import com.zaen.testly.cas.CreateCasData
import com.zaen.testly.data.CardData
import com.zaen.testly.data.FirebaseDocument
import com.zaen.testly.data.SetData
import com.zaen.testly.data.SetData.Companion.SET_CARD_TYPE_MIXED
import com.zaen.testly.data.SetData.Companion.SET_CARD_TYPE_SELECTION
import com.zaen.testly.data.SetData.Companion.SET_CARD_TYPE_SPELLING
import com.zaen.testly.data.SetData.Companion.SET_SUBJECT_TYPE_MIXED
import com.zaen.testly.data.SetData.Companion.SET_SUBJECT_TYPE_SINGLE
import com.zaen.testly.data.SetData.Companion.SET_TYPE_CHECK
import com.zaen.testly.data.UserData
import com.zaen.testly.views.recyclers.items.SelectCardItem
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.SelectableAdapter
import eu.davidea.flexibleadapter.common.SmoothScrollGridLayoutManager
import eu.davidea.flexibleadapter.helpers.ActionModeHelper
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.viewholders.FlexibleViewHolder
import kotlinx.android.synthetic.main.fragment_create_set.*

class CreateSetFragment  : BaseFragment(),
        android.support.v7.view.ActionMode.Callback,
        FlexibleAdapter.OnItemClickListener,
        FlexibleAdapter.OnItemLongClickListener{
    interface SubmitSetListener{
        fun onSubmitSetSuccessful()
    }

    private var mListener: SubmitSetListener? = null

    private var mCreateCas = CreateCasData(this)

    private var setType = SET_TYPE_CHECK

    private var setCardType = SET_CARD_TYPE_MIXED

    private var setSubjectType: String? = SET_SUBJECT_TYPE_MIXED
    private var subjectList: ArrayList<String>? = null

    private var cardSubject: String? = null

    private var selectCardAdapter: FlexibleAdapter<AbstractFlexibleItem<FlexibleViewHolder>>? = null
    private var actionMode: android.support.v7.view.ActionMode? = null
    private var mActionHelper: ActionModeHelper? = null

    private var selectedDocumentList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null){

        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is SubmitSetListener){
            mListener = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layoutRes = R.layout.fragment_create_set
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_fragment_create_set,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null){
            if (selectCardAdapter != null){
                selectCardAdapter!!.onRestoreInstanceState(savedInstanceState)
                mActionHelper?.restoreSelection(activity as AppCompatActivity)
            }
            return
        }

        initializeViews()

        TestlyUser(this).addUserinfoListener(object: TestlyUser.UserinfoListener {
            override fun onUserinfoUpdate(userinfo: UserData?) {
                if (userinfo != null) {
                    val school = userinfo.schoolId
                    val grade = userinfo.gradeId
                    TestlyFirestore(this).addDocumentListener(FirebaseFirestore.getInstance().collection("schools").document(school)
                            .collection("grades").document(grade),object: TestlyFirestore.DocumentListener{
                        override fun handleListener(registration: ListenerRegistration?) {
                        }
                        override fun onDocumentUpdate(path: DocumentReference, snapshot: DocumentSnapshot?, exception: Exception?) {
                            if (snapshot != null) {
                                subjectList = snapshot.get("subjects") as ArrayList<String>
                            } else {
                                // error getting subjects
                            }
                        }
                    })
                }
            }
        })
        initializeAdapter()
        getCards()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        selectCardAdapter?.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    private fun initializeActionModeHelper(mode: Int){
        mActionHelper = ActionModeHelper(selectCardAdapter!!,R.menu.menu_main,this).withDefaultMode(mode)
    }

    override fun onPrepareActionMode(mode: android.support.v7.view.ActionMode?, menu: Menu?): Boolean {
        return true
    }

    override fun onCreateActionMode(mode: android.support.v7.view.ActionMode?, menu: Menu?): Boolean {
        // Change menu?
        selectCardAdapter?.mode = SelectableAdapter.Mode.MULTI
        actionMode = mode
        return true
    }

    override fun onDestroyActionMode(mode: android.support.v7.view.ActionMode?) {
        selectCardAdapter?.mode = SelectableAdapter.Mode.IDLE
        actionMode = null
    }

    override fun onActionItemClicked(mode: android.support.v7.view.ActionMode?, item: MenuItem?): Boolean {
        return true
    }

    override fun onItemClick(view: View?, position: Int): Boolean {
        return if (selectCardAdapter?.mode != SelectableAdapter.Mode.IDLE && mActionHelper != null) {
            toggleSelection(position)
            val activate = mActionHelper!!.onClick(position)
            activate
        } else {
            false
        }
    }

    override fun onItemLongClick(position: Int) {
        mActionHelper?.onLongClick(activity as AppCompatActivity, position)
    }

    private fun initializeViews(){
        radio_btn_set_card_mixed.setOnCheckedChangeListener { buttonView, isChecked ->
            onSetCardTypeClicked(buttonView)
        }
        radio_btn_set_card_selection.setOnCheckedChangeListener { buttonView, isChecked ->
            onSetCardTypeClicked(buttonView)
        }
        radio_btn_set_card_spelling.setOnCheckedChangeListener{ buttonView, isChecked ->
            onSetCardTypeClicked(buttonView)
        }

        check_create_set_card_subject_mixed.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                setSubjectType = SET_SUBJECT_TYPE_MIXED
                spinner_create_set_card_subjects.isEnabled = false
                cardSubject = null
            } else {
                setSubjectType = SET_SUBJECT_TYPE_SINGLE
                spinner_create_set_card_subjects.isEnabled = true
                if (subjectList != null) {
                    val adapter = ArrayAdapter<String>(activity, R.layout.item_spinner_create_set_card_subject, subjectList)
                    spinner_create_set_card_subjects.setAdapter(adapter)
                    spinner_create_set_card_subjects.setOnItemSelectedListener { view, position, id, item ->
                        onSetCardSubjectSelected(item)
                    }
                }
            }
            updateUI()
        }

    }
    private fun onSetCardTypeClicked(radioButton: CompoundButton){
        when (radioButton.id){
            R.id.radio_btn_set_card_mixed -> {
                setCardType = SET_CARD_TYPE_MIXED
            }
            R.id.radio_btn_set_card_selection -> {
                setCardType = SET_CARD_TYPE_SELECTION
            }
            R.id.radio_btn_set_card_spelling -> {
                setCardType = SET_CARD_TYPE_SPELLING
            }
        }
        updateUI()
    }
    private fun toggleSelection(position: Int){
        val positions = selectCardAdapter!!.selectedPositions
        if (positions.size > 0){
            selectedDocumentList.clear()
            for (p in positions){
                selectedDocumentList.add(mCreateCas.cardList[p].id)
            }
        } else {
            //actionMode?.finish()
        }
    }

    private fun getCards(){
        val orderBy = "timestamp"
        val wheres = HashMap<String,Any?>()

        when (setCardType){
            SET_CARD_TYPE_SELECTION -> {
                wheres["type"] = CardData.CARD_TYPE_SELECTION
                wheres["type"] = CardData.CARD_TYPE_SELECTION_MULTIPLE
                wheres["type"] = CardData.CARD_TYPE_SELECTION_MULTIPLE_ORDERED
            }
            SET_CARD_TYPE_SPELLING -> {
                wheres["type"] = CardData.CARD_TYPE_SPELLING
            }
        }

        if (cardSubject != null){
            wheres["subject"] = cardSubject
        }
        val request = mCreateCas.createCasRequest(FirebaseDocument.CARD,orderBy,wheres)
        mCreateCas.casList.clear()
        mCreateCas.cardList.clear()
        mCreateCas.setList.clear()
        mCreateCas.listenToCard(request!!,object: CreateCasData.CreateCasDataListener{
            override fun onCasData() {
                updateUI()
            }
        })
    }

    private fun getItems(): MutableList<AbstractFlexibleItem<FlexibleViewHolder>>{
        val items: MutableList<AbstractFlexibleItem<FlexibleViewHolder>> = mutableListOf()
        if (mCreateCas.casList.size > 0) {
            val casList = mCreateCas.casList
            casList.reverse()
            for (cas in casList) {
                when (cas){
                    is CardData -> items.add(SelectCardItem(cas))
                }
            }
        }
        return items
    }

    private fun initializeAdapter(){
        val mLayoutManager = SmoothScrollGridLayoutManager(activity,3, LinearLayout.VERTICAL,false)
        selectCardAdapter = FlexibleAdapter(getItems())
        selectCardAdapter?.mode = SelectableAdapter.Mode.MULTI
        selectCardAdapter?.addListener(this)
        recycler_create_set_cards.apply {
            layoutManager = mLayoutManager
            adapter = selectCardAdapter
        }
        initializeActionModeHelper(SelectableAdapter.Mode.MULTI)
    }

    private fun updateUI(){
        selectCardAdapter?.updateDataSet(getItems())
    }

    private fun onSetCardSubjectSelected(item: Any){
        cardSubject = item.toString()
        getCards()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_submit_set -> {
                submitSet()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkError():Boolean{
        input_create_set_title.error = null
        error_create_set_subject_not_selected.visibility = View.GONE
        error_create_set_cards_not_selected.visibility = View.GONE

        var cancel = false
        var focusView: View? = null

        if (selectedDocumentList.size < 1){
            error_create_set_cards_not_selected.visibility = View.VISIBLE
            focusView = recycler_create_set_cards
            cancel = true
        }

        if (setSubjectType == null && cardSubject == null){
            error_create_set_subject_not_selected.visibility = View.VISIBLE
            focusView = spinner_create_set_card_subjects
            cancel = true
        }

        if (TextUtils.isEmpty(edit_create_set_title.text)){
            input_create_set_title.error = activity!!.resources.getString(R.string.error_field_required)
            focusView = edit_create_set_title
            cancel = true
        }

        return if (cancel){
            focusView?.requestFocus()
            true
        } else {
            false
        }
    }

    private fun submitSet(){
        if (checkError()){
            return
        }

        val dialog = MaterialDialog.Builder(activity!!)
                .title("Uploading...")
                .content("")
                .progress(false,1,true)
                .canceledOnTouchOutside(false)
                .show()

        val path = FirebaseFirestore.getInstance().collection("sets").document()
        val set = SetData(
                id = path.id,
                timestamp = System.currentTimeMillis() / 1000L,
                title = edit_create_set_title.text.toString(),
                setType = setType,
                cardType = setCardType,
                subjectType = setSubjectType!!,
                cards = selectedDocumentList
        )
        TestlyFirestore(this).addDocumentToCollection(path,set,object: TestlyFirestore.UploadToCollectionListener{
            override fun onDocumentUpload(path: Query, reference: DocumentReference?, exception: Exception?) {
                if (reference != null){
                    mListener?.onSubmitSetSuccessful()
                    while(dialog.currentProgress < dialog.maxProgress){
                        if (dialog.isCancelled){ break }
                        dialog.incrementProgress(1)
                    }
                }
            }
        })
    }
}